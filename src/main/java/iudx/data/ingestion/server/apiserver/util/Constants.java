package iudx.data.ingestion.server.apiserver.util;

public class Constants {

	// Header params
	public static final String HEADER_TOKEN = "token";
	public static final String HEADER_HOST = "Host";
	public static final String HEADER_ACCEPT = "Accept";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	public static final String HEADER_ORIGIN = "Origin";
	public static final String HEADER_REFERER = "Referer";
	public static final String HEADER_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public static final String HEADER_OPTIONS = "options";

	// NGSI-LD endpoints
	public static final String NGSILD_BASE_PATH = "/ngsi-ld/v1";
	public static final String NGSILD_ENTITIES_URL = NGSILD_BASE_PATH + "/entities";

	// request/response params
	public static final String CONTENT_TYPE = "content-type";
	public static final String APPLICATION_JSON = "application/json";

}
