package eu.ha3.presencefootsteps.config;

public interface Options {
    boolean containsKey(String option);

    <T> T get(String option);

    <T> Options withOption(String option, T value);
}
