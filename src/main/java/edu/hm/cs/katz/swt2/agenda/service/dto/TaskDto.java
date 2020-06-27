package edu.hm.cs.katz.swt2.agenda.service.dto;

import edu.hm.cs.katz.swt2.agenda.common.TaskTypeEnum;

/**
 * Transferobjekt für einfache Anzeigeinformationen von Tasks. Transferobjekte
 * sind Schnittstellenobjekte der Geschäftslogik; Sie sind nicht Teil des
 * Modells, so dass Änderungen an den Transferobjekten die Überprüfungen der
 * Geschäftslogik nicht umgehen können.
 * 
 * @see OwnerTaskDto
 * @see SubscriberTaskDto
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
public class TaskDto {
	Long id;
	String title;
	String taskShortDescription;
	String taskLongDescription;
	TaskTypeEnum taskType;
	SubscriberTopicDto topic;

	/**
	 * Konstruktor.
	 */
	public TaskDto(Long id, String title, String taskShortDescription, String taskLongDescription,
		TaskTypeEnum taskType, SubscriberTopicDto topicDto) {
		this.id = id;
		this.title = title;
		this.taskShortDescription = taskShortDescription;
		this.taskLongDescription = taskLongDescription;
		this.taskType = taskType;
		this.topic = topicDto;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTaskShortDescription() {
		return taskShortDescription;
	}

	public void setTaskShortDescription(String taskShortDescription) {
		this.taskShortDescription = taskShortDescription;
	}

	public String getTaskLongDescription() {
		return taskLongDescription;
	}

	public void setTaskLongDescription(String taskLongDescription) {
		this.taskLongDescription = taskLongDescription;
	}

	public TaskTypeEnum getTaskType() {
		return taskType;
	}

	public void setTaskType(TaskTypeEnum taskType) {
		this.taskType = taskType;
	}

	public SubscriberTopicDto getTopic() {
		return topic;
	}

	public void setTopic(SubscriberTopicDto topic) {
		this.topic = topic;
	}

}
