package edu.hm.cs.katz.swt2.agenda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring-Boot-Application. Spring-Boot-Anwendungen starten eine Rahmenanwendung, die selbständig
 * nach Programmteilen sucht und diese miteinander verbindet bzw. diese für Anfragen aus dem Web
 * konfiguriert. Daher findet sich in der {@link #main(String[])}-Methode nicht mehr als ein Aufruf
 * einer {@link SpringApplication}.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */
@SpringBootApplication
public class AgendaApplication {

  public static void main(String[] args) {
    SpringApplication.run(AgendaApplication.class, args);
  }
}
