package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.repositories.FeatureSampleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class FeatureSampleService {

    private final FeatureSampleRepository featureSampleRepository;

    public FeatureSample save(FeatureSample sample){
        return featureSampleRepository.save(sample);
    }
    public Long getSize() { return featureSampleRepository.count(); }
    public List<FeatureSample> findAll() {
        return featureSampleRepository.findAll();
    }


    public Double[] getTypingSpeedRange() {
        List<FeatureSample> samples = featureSampleRepository.findAllByOrderByTypingSpeedAsc();
        return new Double[] {
                samples.get(0).getTypingSpeed(),
                samples.get(samples.size() - 1).getTypingSpeed()
        };
    }

    public Double[] getFrequencyRange() {
        Double minimum = findAscFrequencyValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscFrequencyValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[] { minimum, maximum };
    }

    public Double[] getTimeCostsRange() {
        Double minimum = findAscTimedValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscTimedValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[] { minimum, maximum };
    }

    public List<Double> findAscFrequencyValues(int index){
        return Stream.of(
                featureSampleRepository.findAllByOrderByMistakesFrequencyAsc().get(index).getMistakesFrequency(),
                featureSampleRepository.findAllByOrderByNumPadUsageFrequencyAsc().get(index).getNumPadUsageFrequency()
        ).collect(Collectors.toList());
    }
    public List<Double> findAscTimedValues(int index){
        return Stream.of(
                featureSampleRepository.findAllByOrderByMeanDelBackspDwellAsc().get(index).getMeanDelBackspDwell(),
                featureSampleRepository.findAllByOrderByMeanDwellTimeAsc().get(index).getMeanDwellTime(),
                featureSampleRepository.findAllByOrderByMeanDigraphKUTimeAsc().get(index).getMeanDigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanDigraphKDTimeAsc().get(index).getMeanDigraphKDTime(),
                featureSampleRepository.findAllByOrderByMeanTrigraphKUTimeAsc().get(index).getMeanTrigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanTrigraphKDTimeAsc().get(index).getMeanTrigraphKDTime())
                .collect(Collectors.toList());

    }
}
