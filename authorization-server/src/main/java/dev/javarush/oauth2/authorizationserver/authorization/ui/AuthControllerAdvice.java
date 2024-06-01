package dev.javarush.oauth2.authorizationserver.authorization.ui;

import dev.javarush.oauth2.authorizationserver.authorization.InvalidAuthRequestException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class AuthControllerAdvice {

  @ExceptionHandler(InvalidAuthRequestException.class)
  public ModelAndView handleInvalidAuthRequest(InvalidAuthRequestException ex, Model model) {
    if (ex.getRedirectUri() != null) {
      model.addAttribute("error", ex.getErrorCode());
      model.addAttribute("error_description", ex.getErrorDescription());
      return new ModelAndView("redirect:" + ex.getRedirectUri(), model.asMap());
    }
    model.addAttribute("error", ex.getLocalizedMessage());
    return new ModelAndView("error", model.asMap());
  }

}
