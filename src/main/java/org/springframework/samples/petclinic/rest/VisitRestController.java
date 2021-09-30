/*
 * Copyright 2016-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Vitaliy Fedoriv
 *
 */

/*
 * It is not a good idea to use DB entities in controller, Data Transfer Object should be used instead.
 * - it will decouple REST service contract from it implementation and future DB model changes won't affect the contract
 * - it will make REST contract more useful for end users as we can hide technical fields from them
 * - it will require one more type of Spring beans to implement - mappers. It will increase the difficulty of the
 *   entire application, but it is the reasonable price for first two improvements
 */
@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api/visits")
public class VisitRestController {

    /*
     * We should use constructor-based autowiring instead of field autowiring as it hase some drawbacks
     */
	@Autowired
	private ClinicService clinicService;

    @PreAuthorize( "hasRole(@roles.OWNER_ADMIN)" )
	@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Collection<Visit>> getAllVisits(){
		Collection<Visit> visits = new ArrayList<Visit>();
		visits.addAll(this.clinicService.findAllVisits());
		if (visits.isEmpty()){
			return new ResponseEntity<Collection<Visit>>(HttpStatus.NOT_FOUND);
		}
        /*
         * We can use diamond syntax to create ResponseEntity, if we use java with version number grater then 1.6
         */
		return new ResponseEntity<Collection<Visit>>(visits, HttpStatus.OK);
	}

    @PreAuthorize( "hasRole(@roles.OWNER_ADMIN)" )
	@RequestMapping(value = "/{visitId}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Visit> getVisit(@PathVariable("visitId") int visitId){
		Visit visit = this.clinicService.findVisitById(visitId);
		if(visit == null){
			return new ResponseEntity<Visit>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Visit>(visit, HttpStatus.OK);
	}

    @PreAuthorize( "hasRole(@roles.OWNER_ADMIN)" )
	@RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Visit> addVisit(@RequestBody @Valid Visit visit, BindingResult bindingResult, UriComponentsBuilder ucBuilder){
		BindingErrorsResponse errors = new BindingErrorsResponse();
		HttpHeaders headers = new HttpHeaders();
		if(bindingResult.hasErrors() || (visit == null) || (visit.getPet() == null) || visit.getVet() == null){
			errors.addAllErrors(bindingResult);
			headers.add("errors", errors.toJSON());
			return new ResponseEntity<Visit>(headers, HttpStatus.BAD_REQUEST);
        }
        /*
         * This row will go away if we will use primitive boolean for `paid` field
         */
		visit.setPaid(false);
        this.clinicService.saveVisit(visit);
		headers.setLocation(ucBuilder.path("/api/visits/{id}").buildAndExpand(visit.getId()).toUri());
		return new ResponseEntity<Visit>(visit, headers, HttpStatus.CREATED);
	}

    @PreAuthorize( "hasRole(@roles.OWNER_ADMIN)" )
	@RequestMapping(value = "/{visitId}", method = RequestMethod.PUT, produces = "application/json")
	public ResponseEntity<Visit> updateVisit(@PathVariable("visitId") int visitId, @RequestBody @Valid Visit visit, BindingResult bindingResult){
		BindingErrorsResponse errors = new BindingErrorsResponse();
		HttpHeaders headers = new HttpHeaders();
		if(bindingResult.hasErrors() || (visit == null) || (visit.getPet() == null)){
			errors.addAllErrors(bindingResult);
			headers.add("errors", errors.toJSON());
			return new ResponseEntity<Visit>(headers, HttpStatus.BAD_REQUEST);
		}
		Visit currentVisit = this.clinicService.findVisitById(visitId);
		if(currentVisit == null){
			return new ResponseEntity<Visit>(HttpStatus.NOT_FOUND);
		}
		currentVisit.setDate(visit.getDate());
		currentVisit.setDescription(visit.getDescription());
		currentVisit.setPet(visit.getPet());
        currentVisit.setAdHoc(visit.getAdHoc());
        currentVisit.setScheduled(visit.getScheduled());
		this.clinicService.saveVisit(currentVisit);
        /*
         * Return NO_CONTENT code here is incorrect, it have to be OK
         */
		return new ResponseEntity<Visit>(currentVisit, HttpStatus.NO_CONTENT);
	}

    @PreAuthorize( "hasRole(@roles.OWNER_ADMIN)" )
	@RequestMapping(value = "/{visitId}", method = RequestMethod.DELETE, produces = "application/json")
    /*
     * We shouldn't use @Transactional on controller method. Instead, we should use it on service method
     */
	@Transactional
	public ResponseEntity<Void> deleteVisit(@PathVariable("visitId") int visitId){
		Visit visit = this.clinicService.findVisitById(visitId);
		if(visit == null){
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		this.clinicService.deleteVisit(visit);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}

    @PreAuthorize( "hasRole(@roles.VET_ADMIN)" )
    /*
     * It is preferable to use verb in REST method name, for example `/{visitId}/pay` or `/{visitId}/do-pay`.
     * It is more correctly to use http method PATCH here as we will change only one attribute of whole Visit entity.
     */
    @RequestMapping(value = "/{visitId}/payment", method = RequestMethod.PUT, produces = "application/json")
    public ResponseEntity<Visit> markPaid(@PathVariable("visitId") int visitId){
        Visit currentVisit = this.clinicService.findVisitById(visitId);
        if(currentVisit == null){
            return new ResponseEntity<Visit>(HttpStatus.NOT_FOUND);
        }
        currentVisit.setPaid(true);
        this.clinicService.saveVisit(currentVisit);
        /*
         * We should return response with changed entity here, as method signature demands it from us
         */
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
