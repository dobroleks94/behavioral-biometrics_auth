package acs.behavioral_biometrics.user_keystroke_profile.service;

import acs.behavioral_biometrics.keystroke_handler.services.KeyProfileSamplesService;
import acs.behavioral_biometrics.user_keystroke_profile.enums.Feature;
import acs.behavioral_biometrics.user_keystroke_profile.model.FeatureSample;
import acs.behavioral_biometrics.user_keystroke_profile.repository.FeatureSampleRepository;
import acs.behavioral_biometrics.user_keystroke_profile.utils.KeystrokeDataToFeaturesConverter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.DoubleStream;

@Service
@AllArgsConstructor
public class FeatureSampleService {

    private final FeatureSampleRepository featureSampleRepository;
    private final KeyProfileSamplesService kpsService;
    private final KeystrokeDataToFeaturesConverter converter;

    public FeatureSample save(FeatureSample sample) {
        return featureSampleRepository.save(sample);
    }
    public long getCount() { return featureSampleRepository.count(); }
    public List<FeatureSample> findAll() {
        return featureSampleRepository.findAll();
    }

    public FeatureSample buildFeatureSample() {
        if (kpsService.getSamplesCollector().size() > 0) {
            FeatureSample featureSample = FeatureSample.createFeatureSample(
                    converter.fillFeatures(kpsService.getSamplesCollector(), kpsService.getKeyProfilesCollector())
            );
            kpsService.clearAllContainers();
            return featureSample;
        }
        else
            throw new RuntimeException("It is likely to have been input nothing :(");
    }

    public Double chooseCrispValueFrom(FeatureSample featureSample, Feature feature) {
        return switch (feature) {
            case TYPING_SPEED -> featureSample.getTypingSpeed();
            case MEAN_DWELL_TIME -> featureSample.getMeanDwellTime();
            case MEAN_DEL_BACKSP_DWELL -> featureSample.getMeanDelBackspDwell();
            case MEAN_FLIGHT_TIME -> featureSample.getMeanFlightTime();
            case MEAN_DIGRAPH_KU_TIME -> featureSample.getMeanDigraphKUTime();
            case MEAN_DIGRAPH_KD_TIME -> featureSample.getMeanDigraphKDTime();
            case MEAN_TRIGRAPH_KU_TIME -> featureSample.getMeanTrigraphKUTime();
            case MEAN_TRIGRAPH_KD_TIME -> featureSample.getMeanTrigraphKDTime();
            case MISTAKES_FREQUENCY -> featureSample.getMistakesFrequency();
            case NUMPAD_USAGE_FREQUENCY -> featureSample.getNumPadUsageFrequency();
        };
    }

    public DoubleStream findFrequencyValues() {
        List<FeatureSample> samples = featureSampleRepository.findAll();
        return DoubleStream.concat(
                samples.stream().mapToDouble(FeatureSample::getMistakesFrequency),
                samples.stream().mapToDouble(FeatureSample::getNumPadUsageFrequency)
        );
    }
    public DoubleStream findDwellTimeValues() {
        List<FeatureSample> samples = featureSampleRepository.findAll();
        return DoubleStream.concat(
                samples.stream().mapToDouble(FeatureSample::getMeanDelBackspDwell),
                samples.stream().mapToDouble(FeatureSample::getMeanDwellTime)
        );

    }
    public DoubleStream findFlightTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanFlightTime);
    }
    public DoubleStream findTypingSpeedValues(){
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getTypingSpeed);
    }
    public DoubleStream findDigraphKUTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanDigraphKUTime);
    }
    public DoubleStream findDigraphKDTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanDigraphKDTime);
    }
    public DoubleStream findTrigraphKUTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanTrigraphKUTime);
    }
    public DoubleStream findTrigraphKDTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanTrigraphKDTime);
    }
}
