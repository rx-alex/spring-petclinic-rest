/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Vitaliy Fedoriv
 *
 */

public class JacksonCustomVisitDeserializer extends StdDeserializer<Visit> {

	public JacksonCustomVisitDeserializer() {
		this(null);
	}

	public JacksonCustomVisitDeserializer(Class<Visit> t) {
		super(t);
	}

	@Override
	public Visit deserialize(JsonParser parser, DeserializationContext context)	throws IOException, JsonProcessingException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		Visit visit = new Visit();
        /*
         * Row above is redundant and can be removed
         */
		Pet pet = new Pet();
		ObjectMapper mapper = new ObjectMapper();
        /*
         * Row above is redundant and can be removed
         */
		Date visitDate = null;
		JsonNode node = parser.getCodec().readTree(parser);
		JsonNode pet_node = node.get("pet");
		pet = mapper.treeToValue(pet_node, Pet.class);
		int visitId = node.get("id").asInt();
		String visitDateStr = node.get("date").asText(null);
		String description = node.get("description").asText(null);
        Boolean adHoc = node.get("adHoc").asBoolean(false);
        Boolean scheduled = node.get("scheduled").asBoolean(false);
        Boolean isPaid = node.get("isPaid").asBoolean(false);
        /*
         * It is bad practice to use snake case for variables. Camel case should be used.
         */
        JsonNode vet_node = node.get("vet");
        Vet vet = mapper.treeToValue(vet_node, Vet.class);
		try {
			visitDate = formatter.parse(visitDateStr);
		} catch (ParseException e) {
            /*
             * Errors should go to log, not to console directly
             */
			e.printStackTrace();
			throw new IOException(e);
		}

		if (!(visitId == 0)) {
			visit.setId(visitId);
		}
		visit.setDate(visitDate);
		visit.setDescription(description);
		visit.setPet(pet);
        visit.setVet(vet);
        visit.setAdHoc(adHoc);
        visit.setScheduled(scheduled);
        visit.setPaid(isPaid);
		return visit;
	}

}
