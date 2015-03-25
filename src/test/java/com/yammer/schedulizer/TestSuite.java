package com.yammer.schedulizer;

import com.yammer.schedulizer.config.SchedulizerConfiguration;
import com.yammer.schedulizer.test.TestApplication;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

@RunWith(ClasspathSuite.class)
public class TestSuite {

    @ClassRule
    public static final DropwizardAppRule<SchedulizerConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, "test.yml");
}
