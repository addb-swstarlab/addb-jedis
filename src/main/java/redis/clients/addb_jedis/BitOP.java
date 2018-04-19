package redis.clients.addb_jedis;

public enum BitOP {
  AND, OR, XOR, NOT;

  public final byte[] raw;

  private BitOP() {
    this.raw = redis.clients.addb_jedis.util.SafeEncoder.encode(name());
  }
}
