package com.intel.ruleengine.gearpump.data.rules;

import com.google.common.collect.ImmutableMap;
import com.intel.ruleengine.gearpump.rules.IdGenerator;
import com.intel.ruleengine.gearpump.rules.Operators;
import com.intel.ruleengine.gearpump.rules.RuleCreator;
import com.intel.ruleengine.gearpump.rules.RuleStatus;
import com.intel.ruleengine.gearpump.tasks.messages.Rule;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RulesRowBuilderTest {

    private RulesRowBuilder rulesRowBuilder;
    private static final byte[] columnFamily = Bytes.toBytes("family");
    private static final byte[] columnName = Bytes.toBytes("name");
    private List<Rule> activeRules;
    private List<Rule> notActiveRules;
    private final String componentId1 = IdGenerator.generateId();
    private final String componentId2 = IdGenerator.generateId();
    private final String componentId3 = IdGenerator.generateId();
    private Rule activeRule;
    private Rule notActiveRule;

    @Before
    public void setUp() throws Exception {
        rulesRowBuilder = new RulesRowBuilder(columnFamily, columnName);
        activeRule = RuleCreator.createRuleWithSingleCondition(Operators.EQUAL, "5", componentId1);
        activeRules = Arrays.asList(activeRule);
        notActiveRule = RuleCreator.copyRule(activeRule);
        notActiveRule.setStatus(RuleStatus.DELETED);
        notActiveRules = Arrays.asList(notActiveRule);
    }

    @Test
    public void shouldReturnOnlyInsertRowsIfThereAreOnlyActiveRules() throws Exception {
        ImmutableMap<String, List<Rule>> newComponentsRules = ImmutableMap.of(
                componentId1, activeRules,
                componentId2, activeRules
        );

        rulesRowBuilder
                .withExistingComponentsRules(new HashMap<>())
                .withNewComponentsRules(newComponentsRules)
                .build();

        assertThat(rulesRowBuilder.getRowsToDelete().size(), is(equalTo(0)));
        assertThat(rulesRowBuilder.getRowsToInsert().size(), is(equalTo(2)));
    }

    @Test
    public void shouldReturnOnlyDeleteRowsIfThereAreNoActiveRules() throws Exception {
        ImmutableMap<String, List<Rule>> newComponentsRules = ImmutableMap.of(
                componentId1, notActiveRules,
                componentId2, notActiveRules
        );

        rulesRowBuilder
                .withExistingComponentsRules(new HashMap<>())
                .withNewComponentsRules(newComponentsRules)
                .build();

        assertThat(rulesRowBuilder.getRowsToDelete().size(), is(equalTo(2)));
        assertThat(rulesRowBuilder.getRowsToInsert().size(), is(equalTo(0)));
    }

    @Test
    public void shouldMergeRowsIfThereAreActiveAndNotActiveRules() throws Exception {
        ImmutableMap<String, List<Rule>> newComponentsRules = ImmutableMap.of(
                componentId1, notActiveRules,
                componentId3, activeRules
        );

        ImmutableMap<String, List<Rule>> existingComonentsRules = ImmutableMap.of(
                componentId1, activeRules,
                componentId2, activeRules
        );

        rulesRowBuilder
                .withExistingComponentsRules(existingComonentsRules)
                .withNewComponentsRules(newComponentsRules)
                .build();

        assertThat(rulesRowBuilder.getRowsToDelete().size(), is(equalTo(1)));
        assertThat(rulesRowBuilder.getRowsToInsert().size(), is(equalTo(1)));
    }
}