package com.teknokote.ess.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class TestUtils
{
   public static String readFile(String fileName) throws IOException
   {
      StringBuilder sbuilder = new StringBuilder();
      try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(TestUtils.class.getResourceAsStream(fileName)))))
      {
         String line = bufferedReader.readLine();
         while(line != null)
         {
            sbuilder.append(line);
            line = bufferedReader.readLine();
         }
      }
      return sbuilder.toString();
   }
}
