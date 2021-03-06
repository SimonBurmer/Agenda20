package edu.hm.cs.katz.swt2.agenda.service;

import edu.hm.cs.katz.swt2.agenda.common.TopicComparator;
import edu.hm.cs.katz.swt2.agenda.common.UuidProviderImpl;
import edu.hm.cs.katz.swt2.agenda.persistence.Topic;
import edu.hm.cs.katz.swt2.agenda.persistence.TopicRepository;
import edu.hm.cs.katz.swt2.agenda.persistence.User;
import edu.hm.cs.katz.swt2.agenda.persistence.UserRepository;
import edu.hm.cs.katz.swt2.agenda.service.dto.OwnerTopicDto;
import edu.hm.cs.katz.swt2.agenda.service.dto.SubscriberTopicDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class TopicServiceImpl implements TopicService {

  private static final Logger LOG = LoggerFactory.getLogger(TopicServiceImpl.class);

  @Autowired
  private UuidProviderImpl uuidProvider;

  @Autowired
  private UserRepository anwenderRepository;

  @Autowired
  private TopicRepository topicRepository;

  @Autowired
  private DtoMapper mapper;

  @Override
  @PreAuthorize("#login==authentication.name OR hasRole('ROLE_ADMIN')")
  public String createTopic(String title, String login, String shortDescription,
      String longDescription) {

    LOG.info("Erstelle ein Topic.");
    LOG.debug("\ttitle=\"{}\" login=\"{}\"", title, login);

    ValidationService.topicValidation(title, shortDescription, longDescription);

    String uuid = uuidProvider.getRandomUuid();
    Optional<User> userOptional = anwenderRepository.findById(login);
    User creator = userOptional.orElseThrow();
    Topic topic = new Topic(uuid, title, creator, shortDescription, longDescription);
    topicRepository.save(topic);
    return uuid;
  }

  @Override
  @PreAuthorize("#login==authentication.name")
  public List<OwnerTopicDto> getManagedTopics(String login, String search) {
    LOG.info("Fordere Liste aller verwalteten Topics eines Anwenders an.");
    LOG.debug("\tlogin=\"{}\" search=\"{}\"", login, search);
    Optional<User> creatorOptional = anwenderRepository.findById(login);
    User creator = creatorOptional.orElseThrow();
    List<Topic> managedTopics = topicRepository.findByCreatorOrderByTitleAsc(creator);
    List<OwnerTopicDto> result = new ArrayList<>();
    for (Topic topic : managedTopics) {
      result.add(mapper.createManagedDto(topic));
    }

    result.removeIf(t -> !t.getTitle().toLowerCase().contains(search.toLowerCase()));

    return result;
  }

  @Override
  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  public OwnerTopicDto getManagedTopic(String topicUuid, String login) {
    LOG.info("Fordere ein verwaltetes Topic eines Anwenders an.");
    LOG.debug("\ttopicUuid={} login=\"{}\"", topicUuid, login);
    Topic topic = topicRepository.getOne(topicUuid);
    return mapper.createManagedDto(topic);
  }

  @Override
  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  public SubscriberTopicDto getTopic(String topicUuid, String login) {
    LOG.info("Fordere Topic mit UUID für einen Anwender an.");
    LOG.debug("\ttopicUuid={} login=\"{}\"", topicUuid, login);
    Topic topic = topicRepository.getOne(topicUuid);
    return mapper.createDto(topic, login);
  }

  @Override
  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  public List<SubscriberTopicDto> getSubscribedUsersWithFinishedTasks(String topicUuid,
      String login) {
    LOG.info("Fordere Abbonenten eines Topics an.");
    LOG.debug("\ttopicUuid={} login=\"{}\"", topicUuid, login);
    Topic topic = topicRepository.getOne(topicUuid);
    Collection<User> subscribers = topic.getSubscriber();

    List<SubscriberTopicDto> subscribersWithFinishedTaks = new ArrayList<>();
    for (User u : subscribers) {
      SubscriberTopicDto subscriberTopic = mapper.createDto(topic, u.getLogin());
      subscriberTopic.setSubscriberForTopic(mapper.createDto(u));
      subscribersWithFinishedTaks.add(subscriberTopic);
    }

    subscribersWithFinishedTaks.sort(new Comparator<SubscriberTopicDto>() {
      @Override
      public int compare(SubscriberTopicDto left, SubscriberTopicDto right) {
        return ((Integer) right.getAmountFinishedTasks()).compareTo(left.getAmountFinishedTasks());
      }
    });

    return subscribersWithFinishedTaks;
  }

  @Override
  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  public void subscribe(String topicUuid, String login) {
    LOG.info("Abonniere ein Topic für einen Anwender.");
    LOG.debug("\ttopicUuid={} login=\"{}\"", topicUuid, login);
    Topic topic = topicRepository.getOne(topicUuid);
    User anwender = anwenderRepository.getOne(login);
    topic.register(anwender);
  }

  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  @Override
  public void unsubscribe(String topicUuid, String login) {
    LOG.info("De-Abonniere ein Topic für einen Anwender.");
    LOG.debug("\ttopicUuid={} login=\"{}\"", topicUuid, login);
    Topic topic = topicRepository.getOne(topicUuid);
    User anwender = anwenderRepository.getOne(login);
    topic.unregister(anwender);
  }

  @Override
  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  public List<SubscriberTopicDto> getSubscriptions(String login, String search) {
    LOG.info("Fordere Liste aller abonnierten Topics eines Anwender an.");
    LOG.debug("\tlogin=\"{}\" search=\"{}\"", login, search);
    Optional<User> creatorOptional = anwenderRepository.findById(login);
    User creator = creatorOptional.orElseThrow();
    Collection<Topic> subscriptions = creator.getSubscriptions();

    TopicComparator tpComp = new TopicComparator();
    List<Topic> subscriptionsList = new ArrayList<Topic>(subscriptions);
    Collections.sort(subscriptionsList, tpComp);

    List<SubscriberTopicDto> result = new ArrayList<>();
    for (Topic topic : subscriptionsList) {
      result.add(mapper.createDto(topic, login));
    }

    result.removeIf(t -> !t.getTitle().toLowerCase().contains(search.toLowerCase()));

    return result;
  }

  @Override
  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  public void deleteTopic(String topicUuid, String login) {
    LOG.info("Löschen eins Topics von einem Anwender.");
    LOG.debug("\ttopicUuid={} login=\"{}\"", topicUuid, login);
    Topic topic = topicRepository.getOne(topicUuid);
    User creator = anwenderRepository.getOne(login);
    if (!topic.getCreator().equals(creator)) {
      throw new AccessDeniedException("Kein Zugriff auf das Topic!");
    }
    topicRepository.delete(topic);
  }

  @Override
  @PreAuthorize("#login == authentication.name or hasRole('ROLE_ADMIN')")
  public void updateTopic(String topicUuid, String login, String shortDescription,
      String longDescription) {
    LOG.info("Update ein Topic eines Anwenders.");
    LOG.debug("\ttopicUuid={} login=\"{}\"", topicUuid, login);
    Topic topic = topicRepository.getOne(topicUuid);
    User creator = anwenderRepository.getOne(login);

    if (!topic.getCreator().equals(creator)) {
      throw new AccessDeniedException("Kein Zugriff auf das Topic!");
    }

    ValidationService.topicValidation(shortDescription, longDescription);

    topic.setShortDescription(shortDescription);
    topic.setLongDescription(longDescription);
  }

  @Override
  public String getTopicUuid(String key) {
    LOG.info("Uuid auflösen für Key {}.", key);
    if (key.length() < 8) {
      throw new ValidationException("Kurzschlüssel ist zu kurz. Ein Kurzschlüssel hat 8 Zeichen");
    }

    if (key.length() >= 9) {
      throw new ValidationException("Kurzschlüssel ist zu lang. Ein Kurzschlüssel hat 8 Zeichen");
    }
    Topic topic = topicRepository.findByUuidEndingWith(key);

    if (topic == null) {
      throw new ValidationException("Zu diesem Kurzschlüssel gibt es kein Topic!");
    }
    return topic.getUuid();
  }



  @Override
  public boolean isAlreadyRegisteredSubscriber(String login, String topicUuid) {

    User anwender = anwenderRepository.getOne(login);
    Topic topic = topicRepository.getOne(topicUuid);

    if (topic.getSubscriber().contains(anwender)) {
      throw new ValidationException("Du hast dieses Topic bereits abboniert!");
    }

    return false;
  }
}
