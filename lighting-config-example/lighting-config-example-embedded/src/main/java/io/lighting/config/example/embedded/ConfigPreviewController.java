package io.lighting.config.example.embedded;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Simple view forwarder so testers can open a static preview page without memorizing the HTML path.
 */
@Controller
public class ConfigPreviewController {

    @GetMapping("/preview")
    public String preview() {
        return "forward:/preview.html";
    }
}
