/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import javax.validation.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.rest.JacksonCustomVisitDeserializer;
import org.springframework.samples.petclinic.rest.JacksonCustomVisitSerializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Simple JavaBean domain object representing a visit.
 *
 * @author Ken Krebs
 */
@Entity
@Table(name = "visits")
/*
 * We can remove all two above annotations without any impact to whole application, as Spring can serialize and
 * deserialize JSON objects on-fly
 */
@JsonSerialize(using = JacksonCustomVisitSerializer.class)
@JsonDeserialize(using = JacksonCustomVisitDeserializer.class)
public class Visit extends BaseEntity {

    /**
     * Holds value of property date.
     */
    @Column(name = "visit_date")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy/MM/dd")
    /*
     * Usage of java.time.LocalDateTime instead of java.util.Date is preferable as it is more convenient to operate in processing methods.
     * java.util.Date is outdated class.
     */
    private Date date;

    /**
     * Holds value of property description.
     */
    @NotEmpty
    @Column(name = "description")
    private String description;

    /*
     * All new added fields should have comments, as all old fields have their ones, as it is best practice in our project.
     *
     * Field `scheduled` should use simple boolean type as corresponding field in DB is not nullable. In such case we will
     * have default value `false` for this field without any additional method call.
     */
    @Column(name = "scheduled")
    private Boolean scheduled;

    /*
     * ad_hoc field here is redundant, as it's value is always the opposite value of scheduled field. We can remove this field
     * without any impact.
     */
    @Column(name = "ad_hoc")
    private Boolean adHoc;

    @ManyToOne
    @JoinColumn(name = "vet_id")
    private Vet vet;

    @Column(name = "is_paid")
    private Boolean isPaid;

    /**
     * Holds value of property pet.
     */
    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;


    /**
     * Creates a new instance of Visit for the current date
     */
    public Visit() {
        this.date = new Date();
    }


    /**
     * Getter for property date.
     *
     * @return Value of property date.
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Setter for property date.
     *
     * @param date New value of property date.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for property pet.
     *
     * @return Value of property pet.
     */
    public Pet getPet() {
        return this.pet;
    }

    /**
     * Setter for property pet.
     *
     * @param pet New value of property pet.
     */
    public void setPet(Pet pet) {
        this.pet = pet;
    }

    /*
     * getter method for boolean field should have name started from `is`, isScheduled() for current case
     */
    public Boolean getScheduled() {
        return scheduled;
    }

    public void setScheduled(Boolean scheduled) {
        this.scheduled = scheduled;
    }

    public Boolean getAdHoc() {
        return adHoc;
    }

    public void setAdHoc(Boolean adHoc) {
        this.adHoc = adHoc;
    }

    public Vet getVet() {
        return vet;
    }

    public void setVet(Vet vet) {
        this.vet = vet;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }
}
