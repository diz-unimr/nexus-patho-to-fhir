/* GNU AFFERO GENERAL PUBLIC LICENSE  Version 3 (C)2025 */
package de.unimarburg.diz.nexuspathotofhir.model;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;

@Data
public class PathoTextMapperResult {

  private HashMap<String, String> codeMap;
  private ArrayList<String> identifiersCodeMapArray;

  public PathoTextMapperResult(
      HashMap<String, String> hashMap, ArrayList<String> identifiersCodeMapArray) {
    this.codeMap = hashMap;
    this.identifiersCodeMapArray = identifiersCodeMapArray;
  }
}
