package com.mobileapp.ridertrack;


/**
 * Object Event.
 * It contains all the fields concerning an event, and getters and setters methods.
 */
public class Event {

    private String id;
    private String organizerId;
    private String name;
    private String type;
    private String startingDate;
    private String country;
    private String city;
    private String startingPoint;
    private String _v;
    private String enrollmentClosingAt;
    private String enrollmentOpeningAt;
    private int length;
    private String startingTime;
    private String status;
    private String description;
    private int maxDuration;
    private double price;
    private int maxParticipants;
    private String logo;
    private String createdAt;
    private String updatedAt;
    private String closingDate;
    private String closingTime;

    public Event(){

    }

    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id = id;
    }

    public String getOrganizerId(){
        return this.organizerId;
    }
    public void setOrganizerId(String organizerId){
        this.organizerId = organizerId;
    }

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getType(){
        return this.type;
    }
    public void setType(String type){
        this.type = type;
    }

    public String getStartingDate(){
        return this.startingDate;
    }
    public void setStartingDate(String startingDate){
        this.startingDate = startingDate;
    }

    public String getCountry(){
        return this.country;
    }
    public void setCountry(String country){
        this.country = country;
    }

    public String getCity(){
        return this.city;
    }
    public void setCity(String city){
        this.city = city;
    }

    public String getV(){
        return this._v;
    }
    public void setV(String _v){
        this._v = _v;
    }

    public String getEnrollmentClosingAt(){
        return this.enrollmentClosingAt;
    }
    public void setEnrollmentClosingAt(String enrollmentClosingAt){
        this.enrollmentClosingAt = enrollmentClosingAt;
    }

    public String getEnrollmentOpeningAt(){
        return this.enrollmentOpeningAt;
    }
    public void setEnrollmentOpeningAt(String enrollmentOpeningAt){
        this.enrollmentOpeningAt = enrollmentOpeningAt;
    }

    public int getLength(){
        return this.length;
    }
    public void setLength(int length){
        this.length = length;
    }

    public String getStartingTime(){
        return this.startingTime;
    }
    public void setStartingTime(String startingTime){
        this.startingTime = startingTime;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(String startingPoint) {
        this.startingPoint = startingPoint;
    }

    public String getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(String closingDate) {
        this.closingDate = closingDate;
    }
    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }
}
