package acs.behavioral_biometrics.app_utils.models;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SystemLogger {
    public static final String PRE_PROCESS_RESULT = "Data pre-process has been done successfully.";
    public static final String FREQUENT_PATTERNS_RESULT = "Frequent patterns are generated successfully.";
    public static final String ASSOCIATION_RULES_DATA_RESULT = "Association rules data is generated successfully.";
    public static final String POST_PROCESS_RESULT = "Association rules data has been post-processed and rules are derived successfully.";
    public static final String DELETING_RESULT = "Temporary files deleted successfully.";
    public static final String SAMPLE_SAVE_SUCCESS_RESULT = "Sample saved successfully!";
    public static final String ASSOCIATION_RULES_SAVE_SUCCESS_RESULT = "Association rules saved successfully!";
    public static final String KEY_FEATURE_CONTAINERS_CLEAN = "All key feature containers have been successfully cleaned";


    public void log(String text){
        System.out.println(text);
    }
}
