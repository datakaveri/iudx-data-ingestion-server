package iudx.data.ingestion.server.apiserver.util;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Configuration {
  public static final String NGSILD_BASEPATH = "basePath";
  private static final Logger LOG = LogManager.getLogger(Configuration.class);
  private static final String CONFIG_PATH = "./configs/config-dev.json";
  private static FileSystem fileSystem;
  private static File file;
  private static Vertx vertx;

  /**
   * Get ApiServerVerticle config to retrieve base path from config-dev
   */

  public static JsonObject getConfiguration() {
    vertx = Vertx.vertx();
    fileSystem = vertx.fileSystem();
    file = new File(CONFIG_PATH);
    if (file.exists()) {
      Buffer buffer = fileSystem.readFileBlocking(CONFIG_PATH);
      JsonObject basePathJson = buffer.toJsonObject().getJsonObject("apiConfiguration");
      return basePathJson;
    } else {
      LOG.error("Couldn't read configuration file : " + CONFIG_PATH);
      return null;
    }
  }

  public static String getBasePath() {
    JsonObject jsonObject = getConfiguration();
    if (jsonObject != null) {
      return jsonObject.getString("ngsildBasePath");
    } else {
      return null;
    }
  }
}
