package edu.hm.cs.katz.swt2.agenda.mvc;

import edu.hm.cs.katz.swt2.agenda.service.TopicService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Controller-Klasse für die Landing-Page. Controller reagieren auf Aufrufe von URLs. Sie benennen
 * ein View-Template (Thymeleaf-Vorlage) und stellen Daten zusammen, die darin dargestellt werden.
 * Dafür verwenden Sie Methoden der Service-Schicht.
 * 
 * @author Bastian Katz (mailto: bastian.katz@hm.edu)
 */

@Controller
public class IndexController extends AbstractController {

  @Autowired
  TopicService topicService;
  
  
  /**
   * Erstellt die Login-Page.
   */
  @GetMapping("/login")
  public String login() {
    return "login";
  }
  
  /**
   * Erstellt die Landing-Page.
   */
  @GetMapping("/")
  public String getIndexView(Model model, Authentication auth, HttpServletRequest request) {
    model.addAttribute("registration", new Registration());
    if (auth != null) {
      model.addAttribute("topics", topicService.getSubscriptions(auth.getName(), ""));
    }
    return "index";
  }


  /**
   * Erstellt aus dem Key die komplette Uuid und gibt den Aufruf zum registrieren zurück.
   */
  @PostMapping("/register")
  public String handleRegistrationKey(@ModelAttribute("registration") Registration registration,
      @RequestHeader(value = "referer", required = true) String referer,
      RedirectAttributes redirectAttributes, Authentication auth) {
    String uuid = "";
    String key = registration.getKey();
    String login = auth.getName();
    
    try {
      uuid = topicService.getTopicUuid(key);
      topicService.isAlreadyRegisteredSubscriber(login, uuid);
      redirectAttributes.addFlashAttribute("sucsess", "Topic gefunden!");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:" + referer;
    }

    return "redirect:/topics/" + uuid + "/register";
  }
}
