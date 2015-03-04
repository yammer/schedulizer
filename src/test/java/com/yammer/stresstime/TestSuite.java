package com.yammer.stresstime;

import com.yammer.stresstime.config.StresstimeConfiguration;
import com.yammer.stresstime.test.TestApplication;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

@RunWith(ClasspathSuite.class)
public class TestSuite {

    @ClassRule
    public static final DropwizardAppRule<StresstimeConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, "test.yml");
}
