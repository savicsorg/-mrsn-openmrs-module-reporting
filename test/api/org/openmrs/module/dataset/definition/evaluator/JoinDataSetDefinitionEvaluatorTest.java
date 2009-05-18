package org.openmrs.module.dataset.definition.evaluator;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.dataset.DataSet;
import org.openmrs.module.dataset.SimpleDataSet;
import org.openmrs.module.dataset.column.DataSetColumn;
import org.openmrs.module.dataset.column.SimpleDataSetColumn;
import org.openmrs.module.dataset.definition.DataSetDefinition;
import org.openmrs.module.dataset.definition.DataSetWrappingDataSetDefinition;
import org.openmrs.module.dataset.definition.JoinDataSetDefinition;
import org.openmrs.module.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.evaluation.EvaluationContext;
import org.openmrs.module.report.ReportData;
import org.openmrs.module.report.renderer.TsvReportRenderer;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

public class JoinDataSetDefinitionEvaluatorTest extends BaseModuleContextSensitiveTest {

    /**
     * @see {@link JoinDataSetDefinitionEvaluator#evaluate(DataSetDefinition,EvaluationContext)}
     */
    @Test
    @Verifies(value = "should join two plain datasets correctly", method = "evaluate(DataSetDefinition,EvaluationContext)")
    public void evaluate_shouldJoinTwoPlainDatasetsCorrectly() throws Exception {
        SimpleDataSet left = new SimpleDataSet();
        left.addRow(makeRowHelper("patient_id", 1, "name", "Alice"));
        left.addRow(makeRowHelper("patient_id", 2, "name", "Bob"));
        left.addRow(makeRowHelper("patient_id", 2, "name", "Charles"));
        DataSetDefinition leftDef = new DataSetWrappingDataSetDefinition(left);
        
        SimpleDataSet right = new SimpleDataSet();
        right.addRow(makeRowHelper("patient_id", 1, "encounter_id", 1, "encounter_type", "Registration"));
        right.addRow(makeRowHelper("patient_id", 1, "encounter_id", 2, "encounter_type", "LabOrder"));
        right.addRow(makeRowHelper("patient_id", 2, "encounter_id", 3, "encounter_type", "Registration"));
        right.addRow(makeRowHelper("patient_id", 1, "encounter_id", 4, "encounter_type", "Dispensing"));
        DataSetDefinition rightDef = new DataSetWrappingDataSetDefinition(right);
        
        JoinDataSetDefinition join = new JoinDataSetDefinition(leftDef, "patient.", "patient_id", rightDef, "encounter.", "patient_id");
        DataSet result = Context.getService(DataSetDefinitionService.class).evaluate(join, new EvaluationContext());
        
        ReportData temp = new ReportData();
        Map<String, DataSet> dataSets = new HashMap<String, DataSet>();
        dataSets.put("joined", result);
        temp.setDataSets(dataSets);
        
        TsvReportRenderer renderer = new TsvReportRenderer();
        renderer.render(temp, null, System.out);

        int numRows = 0;
        for (Object row : result) {
            ++numRows;
        }
        Assert.assertTrue("Wrong number of rown", numRows == 3);
    }

    // needs to have an even number of arguments
    private Map<DataSetColumn, Object> makeRowHelper(Object... o) {
        Map<DataSetColumn, Object> ret = new HashMap<DataSetColumn, Object>();
        for (int i = 0; i < o.length; i += 2) {
            ret.put(new SimpleDataSetColumn((String) o[i]), o[i + 1]);
        }
        return ret;
    }
    
}