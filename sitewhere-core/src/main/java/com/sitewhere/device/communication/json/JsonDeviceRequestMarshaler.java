/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.device.communication.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitewhere.rest.model.device.communication.DecodedDeviceRequest;
import com.sitewhere.rest.model.device.communication.DeviceRequest.Type;
import com.sitewhere.rest.model.device.event.request.DeviceAlertCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceCommandResponseCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceLocationCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceMeasurementsCreateRequest;
import com.sitewhere.rest.model.device.event.request.DeviceRegistrationRequest;
import com.sitewhere.rest.model.device.event.request.DeviceStreamDataCreateRequest;
import com.sitewhere.rest.model.device.request.DeviceStreamCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceAlertCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceCommandResponseCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceLocationCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceMeasurementsCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceRegistrationRequest;
import com.sitewhere.spi.device.event.request.IDeviceStreamCreateRequest;
import com.sitewhere.spi.device.event.request.IDeviceStreamDataCreateRequest;

/**
 * Custom marshaler for converting JSON payloads to {@link DecodedDeviceRequest} objects.
 * 
 * @author Derek
 */
public class JsonDeviceRequestMarshaler extends JsonDeserializer<DecodedDeviceRequest<?>> {

	/** Used to map data into an object based on JSON parsing */
	private static ObjectMapper MAPPER = new ObjectMapper();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.
	 * core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public DecodedDeviceRequest<?> deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		JsonNode node = parser.getCodec().readTree(parser);

		// Get type and validate its in the enum.
		JsonNode typeNode = node.get("type");
		if (typeNode == null) {
			throw new JsonMappingException("Event type is required.");
		}
		try {
			Type type = Type.valueOf(typeNode.asText());
			JsonNode request = node.get("request");

			String hardwareId = node.get("hardwareId").asText();
			String originator = node.get("originator").asText();
			return unmarshal(hardwareId, originator, type, request);
		} catch (IllegalArgumentException e) {
			throw new JsonMappingException("Event type is not valid.");
		}
	}

	/**
	 * Unmarshal payload based on type indicator.
	 * 
	 * @param parser
	 * @param type
	 * @param json
	 * @return
	 * @throws JsonProcessingException
	 */
	protected DecodedDeviceRequest<?> unmarshal(String hardwareId, String originator, Type type,
			JsonNode json) throws JsonProcessingException {
		switch (type) {
		case RegisterDevice: {
			DecodedDeviceRequest<IDeviceRegistrationRequest> decoded =
					new DecodedDeviceRequest<IDeviceRegistrationRequest>();
			decoded.setHardwareId(hardwareId);
			decoded.setOriginator(originator);
			IDeviceRegistrationRequest req = MAPPER.treeToValue(json, DeviceRegistrationRequest.class);
			decoded.setRequest(req);
			return decoded;
		}
		case DeviceLocation: {
			DecodedDeviceRequest<IDeviceLocationCreateRequest> decoded =
					new DecodedDeviceRequest<IDeviceLocationCreateRequest>();
			decoded.setHardwareId(hardwareId);
			decoded.setOriginator(originator);
			IDeviceLocationCreateRequest req = MAPPER.treeToValue(json, DeviceLocationCreateRequest.class);
			decoded.setRequest(req);
			return decoded;
		}
		case DeviceMeasurements: {
			DecodedDeviceRequest<IDeviceMeasurementsCreateRequest> decoded =
					new DecodedDeviceRequest<IDeviceMeasurementsCreateRequest>();
			decoded.setHardwareId(hardwareId);
			decoded.setOriginator(originator);
			IDeviceMeasurementsCreateRequest req =
					MAPPER.treeToValue(json, DeviceMeasurementsCreateRequest.class);
			decoded.setRequest(req);
			return decoded;
		}
		case DeviceAlert: {
			DecodedDeviceRequest<IDeviceAlertCreateRequest> decoded =
					new DecodedDeviceRequest<IDeviceAlertCreateRequest>();
			decoded.setHardwareId(hardwareId);
			decoded.setOriginator(originator);
			IDeviceAlertCreateRequest req = MAPPER.treeToValue(json, DeviceAlertCreateRequest.class);
			decoded.setRequest(req);
			return decoded;
		}
		case DeviceStream: {
			DecodedDeviceRequest<IDeviceStreamCreateRequest> decoded =
					new DecodedDeviceRequest<IDeviceStreamCreateRequest>();
			decoded.setHardwareId(hardwareId);
			decoded.setOriginator(originator);
			IDeviceStreamCreateRequest req = MAPPER.treeToValue(json, DeviceStreamCreateRequest.class);
			decoded.setRequest(req);
			return decoded;
		}
		case DeviceStreamData: {
			DecodedDeviceRequest<IDeviceStreamDataCreateRequest> decoded =
					new DecodedDeviceRequest<IDeviceStreamDataCreateRequest>();
			decoded.setHardwareId(hardwareId);
			decoded.setOriginator(originator);
			IDeviceStreamDataCreateRequest req =
					MAPPER.treeToValue(json, DeviceStreamDataCreateRequest.class);
			decoded.setRequest(req);
			return decoded;
		}
		case Acknowledge: {
			DecodedDeviceRequest<IDeviceCommandResponseCreateRequest> decoded =
					new DecodedDeviceRequest<IDeviceCommandResponseCreateRequest>();
			decoded.setHardwareId(hardwareId);
			decoded.setOriginator(originator);
			IDeviceCommandResponseCreateRequest req =
					MAPPER.treeToValue(json, DeviceCommandResponseCreateRequest.class);
			decoded.setRequest(req);
			return decoded;
		}
		default: {
			throw new JsonMappingException("Unhandled event type: " + type.name());
		}
		}
	}
}