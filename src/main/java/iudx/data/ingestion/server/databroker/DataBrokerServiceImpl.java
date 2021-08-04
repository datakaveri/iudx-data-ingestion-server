package iudx.data.ingestion.server.databroker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class DataBrokerServiceImpl implements DataBrokerService {

	private static final Logger LOGGER = LogManager.getLogger(DataBrokerServiceImpl.class);

	@Override
	public DataBrokerService publishData(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
		// TODO Auto-generated method stub
        handler.handle(Future.succeededFuture(new JsonObject().put("type", "success")));
		return this;
	}

}
