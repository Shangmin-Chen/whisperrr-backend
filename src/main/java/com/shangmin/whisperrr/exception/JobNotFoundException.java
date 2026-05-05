package com.shangmin.whisperrr.exception;

/** Raised when a job id is missing or not visible to the authenticated user (404). */
public class JobNotFoundException extends RuntimeException {

  public JobNotFoundException(String message) {
    super(message);
  }
}
