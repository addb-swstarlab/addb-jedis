package redis.clients.addb_jedis;

public abstract class Builder<T> {
  public abstract T build(Object data);
}
