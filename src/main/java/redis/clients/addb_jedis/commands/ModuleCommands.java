package redis.clients.addb_jedis.commands;

import redis.clients.addb_jedis.Module;

import java.util.List;

public interface ModuleCommands {
  String moduleLoad(String path);
  String moduleUnload(String name);
  List<Module> moduleList();
}
