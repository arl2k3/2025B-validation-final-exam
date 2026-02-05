package com.bezkoder.spring.jpa.h2.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring.jpa.h2.model.Tutorial;
import com.bezkoder.spring.jpa.h2.repository.TutorialRepository;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class TutorialController {

  private static final Logger logger = LoggerFactory.getLogger(TutorialController.class);
  
  private final TutorialRepository tutorialRepository;

  public TutorialController(TutorialRepository tutorialRepository) {
    this.tutorialRepository = tutorialRepository;
  }


  @GetMapping("/tutorials")
  public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(required = false) String title) {
    try {
      List<Tutorial> tutorials = new ArrayList<>();

      if (title == null) {
        tutorialRepository.findAll().forEach(tutorials::add);
      } else {
        tutorialRepository.findByTitleContainingIgnoreCase(title).forEach(tutorials::add);
      }

      if (tutorials.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<>(tutorials, HttpStatus.OK);
    } catch (DataAccessException e) {
      logger.error("Error retrieving tutorials", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @GetMapping("/tutorials/{id}")
  public ResponseEntity<Tutorial> getTutorialById(@PathVariable("id") long id) {
    Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

    return tutorialData
        .map(tutorial -> new ResponseEntity<>(tutorial, HttpStatus.OK))
        .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }


  @PostMapping("/tutorials")
  public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial) {
    try {
      Tutorial createdTutorial = tutorialRepository.save(
          new Tutorial(tutorial.getTitle(), tutorial.getDescription(), false));
      logger.info("Tutorial created with ID: {}", createdTutorial.getId());
      return new ResponseEntity<>(createdTutorial, HttpStatus.CREATED);
    } catch (DataAccessException e) {
      logger.error("Error creating tutorial", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @PutMapping("/tutorials/{id}")
  public ResponseEntity<Tutorial> updateTutorial(@PathVariable("id") long id, @RequestBody Tutorial tutorial) {
    Optional<Tutorial> tutorialData = tutorialRepository.findById(id);

    if (tutorialData.isPresent()) {
      Tutorial existingTutorial = tutorialData.get();
      existingTutorial.setTitle(tutorial.getTitle());
      existingTutorial.setDescription(tutorial.getDescription());
      existingTutorial.setPublished(tutorial.isPublished());
      Tutorial updatedTutorial = tutorialRepository.save(existingTutorial);
      logger.info("Tutorial updated with ID: {}", id);
      return new ResponseEntity<>(updatedTutorial, HttpStatus.OK);
    } else {
      logger.warn("Tutorial not found with ID: {}", id);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/tutorials/{id}")
  public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
    try {
      tutorialRepository.deleteById(id);
      logger.info("Tutorial deleted with ID: {}", id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (DataAccessException e) {
      logger.error("Error deleting tutorial with ID: {}", id, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/tutorials")
  public ResponseEntity<HttpStatus> deleteAllTutorials() {
    try {
      tutorialRepository.deleteAll();
      logger.info("All tutorials deleted");
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (DataAccessException e) {
      logger.error("Error deleting all tutorials", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/tutorials/published")
  public ResponseEntity<List<Tutorial>> findByPublished() {
    try {
      List<Tutorial> tutorials = tutorialRepository.findByPublished(true);

      if (tutorials.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(tutorials, HttpStatus.OK);
    } catch (DataAccessException e) {
      logger.error("Error retrieving published tutorials", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
