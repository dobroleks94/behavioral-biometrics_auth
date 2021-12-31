package acs.behavioral_biometrics.fuzzy_profile_mapper.services;

import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyFeatureSample;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyMeasureItem;
import acs.behavioral_biometrics.fuzzy_profile_mapper.repository.FuzzyFeatureSampleRepository;
import acs.behavioral_biometrics.user_keystroke_profile.model.FeatureSample;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class FuzzyFeatureSampleService {

    private final FuzzyFeatureSampleRepository fuzzyFeatureSampleRepository;
    private final FuzzyFeatureSampleMapper fuzzyMapper;


    public List<FuzzyFeatureSample> saveAll(List<FeatureSample> featureSamples) {
        return fuzzyFeatureSampleRepository.saveAll(fuzzyMapper.getFuzzyRepresentation(featureSamples));
    }
    @Transactional
    public void deleteAllByUserId(Long userId) { fuzzyFeatureSampleRepository.deleteAllByUserId(userId);}

    public void accumulateFuzzyMeasures(List<FuzzyMeasureItem> measureItems){
        fuzzyMapper.setMeasures(measureItems);
    }

}
