package redis.clients.addb_jedis;

import redis.clients.addb_jedis.exceptions.JedisAskDataException;
import redis.clients.addb_jedis.exceptions.JedisClusterMaxAttemptsException;
import redis.clients.addb_jedis.exceptions.JedisClusterOperationException;
import redis.clients.addb_jedis.exceptions.JedisConnectionException;
import redis.clients.addb_jedis.exceptions.JedisMovedDataException;
import redis.clients.addb_jedis.exceptions.JedisNoReachableClusterNodeException;
import redis.clients.addb_jedis.exceptions.JedisRedirectionException;
import redis.clients.addb_jedis.util.JedisClusterCRC16;

public abstract class JedisClusterCommand<T> {

  private static final String NO_DISPATCH_MESSAGE = "No way to dispatch this command to Redis Cluster.";

  private final JedisClusterConnectionHandler connectionHandler;
  private final int maxAttempts;
  private final ThreadLocal<Jedis> askConnection = new ThreadLocal<Jedis>();

  public JedisClusterCommand(JedisClusterConnectionHandler connectionHandler, int maxAttempts) {
    this.connectionHandler = connectionHandler;
    this.maxAttempts = maxAttempts;
  }

  public abstract T execute(Jedis connection);

  public T run(String key) {
    if (key == null) {
      throw new JedisClusterOperationException(NO_DISPATCH_MESSAGE);
    }

    return runWithRetries(JedisClusterCRC16.getSlot(key), this.maxAttempts, false, false);
  }

  public T run(int keyCount, String... keys) {
    if (keys == null || keys.length == 0) {
      throw new JedisClusterOperationException(NO_DISPATCH_MESSAGE);
    }

    // For multiple keys, only execute if they all share the same connection slot.
    int slot = JedisClusterCRC16.getSlot(keys[0]);
    if (keys.length > 1) {
      for (int i = 1; i < keyCount; i++) {
        int nextSlot = JedisClusterCRC16.getSlot(keys[i]);
        if (slot != nextSlot) {
          throw new JedisClusterOperationException("No way to dispatch this command to Redis "
              + "Cluster because keys have different slots.");
        }
      }
    }

    return runWithRetries(slot, this.maxAttempts, false, false);
  }

  public T runBinary(byte[] key) {
    if (key == null) {
      throw new JedisClusterOperationException(NO_DISPATCH_MESSAGE);
    }

    return runWithRetries(JedisClusterCRC16.getSlot(key), this.maxAttempts, false, false);
  }

  public T runBinary(int keyCount, byte[]... keys) {
    if (keys == null || keys.length == 0) {
      throw new JedisClusterOperationException(NO_DISPATCH_MESSAGE);
    }

    // For multiple keys, only execute if they all share the same connection slot.
    int slot = JedisClusterCRC16.getSlot(keys[0]);
    if (keys.length > 1) {
      for (int i = 1; i < keyCount; i++) {
        int nextSlot = JedisClusterCRC16.getSlot(keys[i]);
        if (slot != nextSlot) {
          throw new JedisClusterOperationException("No way to dispatch this command to Redis "
              + "Cluster because keys have different slots.");
        }
      }
    }

    return runWithRetries(slot, this.maxAttempts, false, false);
  }

  public T runWithAnyNode() {
    Jedis connection = null;
    try {
      connection = connectionHandler.getConnection();
      return execute(connection);
    } catch (JedisConnectionException e) {
      throw e;
    } finally {
      releaseConnection(connection);
    }
  }

  private T runWithRetries(final int slot, int attempts, boolean tryRandomNode, boolean asking) {
    if (attempts <= 0) {
      throw new JedisClusterMaxAttemptsException("No more cluster attempts left.");
    }

    Jedis connection = null;
    try {

      if (asking) {
        // TODO: Pipeline asking with the original command to make it
        // faster....
        connection = askConnection.get();
        connection.asking();

        // if asking success, reset asking flag
        asking = false;
      } else {
        if (tryRandomNode) {
          connection = connectionHandler.getConnection();
        } else {
          connection = connectionHandler.getConnectionFromSlot(slot);
        }
      }

      return execute(connection);

    } catch (JedisNoReachableClusterNodeException jnrcne) {
      throw jnrcne;
    } catch (JedisConnectionException jce) {
      // release current connection before recursion
      releaseConnection(connection);
      connection = null;

      if (attempts <= 1) {
        //We need this because if node is not reachable anymore - we need to finally initiate slots renewing,
        //or we can stuck with cluster state without one node in opposite case.
        //But now if maxAttempts = 1 or 2 we will do it too often. For each time-outed request.
        //TODO make tracking of successful/unsuccessful operations for node - do renewing only
        //if there were no successful responses from this node last few seconds
        this.connectionHandler.renewSlotCache();
      }

      return runWithRetries(slot, attempts - 1, tryRandomNode, asking);
    } catch (JedisRedirectionException jre) {
      // if MOVED redirection occurred,
      if (jre instanceof JedisMovedDataException) {
        // it rebuilds cluster's slot cache
        // recommended by Redis cluster specification
        this.connectionHandler.renewSlotCache(connection);
      }

      // release current connection before recursion or renewing
      releaseConnection(connection);
      connection = null;

      if (jre instanceof JedisAskDataException) {
        asking = true;
        askConnection.set(this.connectionHandler.getConnectionFromNode(jre.getTargetNode()));
      } else if (jre instanceof JedisMovedDataException) {
      } else {
        throw new JedisClusterOperationException(jre);
      }

      return runWithRetries(slot, attempts - 1, false, asking);
    } finally {
      releaseConnection(connection);
    }
  }

  private void releaseConnection(Jedis connection) {
    if (connection != null) {
      connection.close();
    }
  }

}
