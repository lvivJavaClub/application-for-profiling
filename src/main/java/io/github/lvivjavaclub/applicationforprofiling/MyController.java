package io.github.lvivjavaclub.applicationforprofiling;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Random;

@Controller("/")
public class MyController {

  HashMap STORAGE = new HashMap<String, Integer>(1, 0.000_9f);
  String CHARS = "0123456789ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";

  @GetMapping("/snail")
  @ResponseBody
  public String snail() {
    Random seed = new Random();
    try {
      Thread.sleep(seed.nextInt(5000));
    } catch (InterruptedException e) {

    }
    return generate();
  }


  @GetMapping("/waitForSnail")
  @ResponseBody
  public String waitForSnail() {
    OkHttpClient client = new OkHttpClient();

      Request request = new Request.Builder()
              .url("http://localhost:8080/snail")
              .build();

      try (Response response = client.newCall(request).execute()) {
        return response.body().string();
      } catch (IOException e) {

      }

    return "error";
  }

  @GetMapping("/generate")
  @ResponseBody
  public String generateNewPassword() {
    return generate();
  }

  /**
   * DO NOT USER!!! It is code for code profiling!
   */
  private String generate() {
    String pass = "";
    for (int i = 0; i < 10; i++) {
      synchronized (CHARS) {
        for (int j = 0; j < 50; j++) {
          Random seed = new Random(j);
          Random rand = new Random(seed.nextLong());
          int n = rand.nextInt(CHARS.length());

          STORAGE.put(pass, (Integer) STORAGE.getOrDefault(pass, 0) + 1);
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
          }
          pass = pass + "" + CHARS.charAt(n);
        }
      }
    }
    return pass;
  }

  @Async
  @Scheduled(initialDelay = 1000, fixedDelay = 1000)
  public void initialDelay1() {
    for (int i = 0; i < 3; i++) {
      generate();
    }
    System.out.println("initialDelay1");
  }

  @Scheduled(initialDelay = 2000, fixedDelay = 1000)
  public void initialDelay2() {
    String result = "";
    for (int i = 0; i < 5; i++) {
      result += generate();
    }

    try {
      File file = File.createTempFile("profiling-test", "txt");
      System.out.println(Thread.currentThread().getName() + " - " + file.getAbsolutePath());
      Files.write(file.toPath(), result.getBytes());
    } catch (IOException e) {

    }

    System.out.println("initialDelay2");
  }

}
