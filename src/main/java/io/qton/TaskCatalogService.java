package io.qton;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import io.quarkus.cli.plugin.CatalogService;

public class TaskCatalogService extends CatalogService<TaskCatalog> {

  private static final Function<Path, Path> RELATIVE_PLUGIN_CATALOG = p -> p.resolve(".qton").resolve("tasks");

  public TaskCatalogService() {
    super(TaskCatalog.class, GIT_ROOT, RELATIVE_PLUGIN_CATALOG);
  }

  @Override
  public TaskCatalog readCatalog(Path path) {
    return new TaskCatalog(Optional.of(path).map(RELATIVE_PLUGIN_CATALOG));
  }

}
