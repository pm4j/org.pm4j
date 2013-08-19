package org.pm4j.req_domain.pm;

import junit.framework.TestCase;

import org.pm4j.core.event.impl.RecordingTestListener;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.req_domain.model.Requirement;
import org.pm4j.req_domain.pm.impl.RequirementPmImpl;

public class RequirementPmTest extends TestCase {

	public void testPmGetSet() {
		PmConversation pmConversation = new PmConversationImpl(RequirementPmImpl.class);
		Requirement requirement = new Requirement();
		RequirementPm requirementPm = PmFactoryApi.getPmForBean(pmConversation, requirement);

		requirementPm.getName().setValue("Hallo");

		assertEquals("Hallo", requirement.getName());
		assertEquals("Hallo", requirementPm.getName().getValue());
	}

	public void testPmEvents() {
	  PmConversation pmConversation = new PmConversationImpl(RequirementPmImpl.class);
    RequirementPm requirementPm = PmFactoryApi.getPmForBean(pmConversation, new Requirement());
    PmAttrString nameAttr = requirementPm.getName();

		RecordingTestListener valueChangeListener = new RecordingTestListener();
    RecordingTestListener changedStateChangeListener = new RecordingTestListener();
    RecordingTestListener allEventsListener = new RecordingTestListener();

    PmEventApi.addPmEventListener(nameAttr, PmEvent.VALUE_CHANGE, valueChangeListener);
    PmEventApi.addPmEventListener(nameAttr, PmEvent.VALUE_CHANGED_STATE_CHANGE, changedStateChangeListener);
    PmEventApi.addPmEventListener(nameAttr, PmEvent.ALL, allEventsListener);

    assertEquals(false, nameAttr.isPmValueChanged());

    nameAttr.setValue("Hallo");

    assertEquals(true, nameAttr.isPmValueChanged());
		assertEquals(1, valueChangeListener.getEventCount());
    assertEquals(1, changedStateChangeListener.getEventCount());
    assertEquals(2, allEventsListener.getEventCount());

    nameAttr.setValue("Hallo-2");

    assertEquals(true, nameAttr.isPmValueChanged());
    assertEquals(2, valueChangeListener.getEventCount());
    assertEquals(1, changedStateChangeListener.getEventCount());
    assertEquals(3, allEventsListener.getEventCount());

    nameAttr.setValue(null);
    assertEquals(false, nameAttr.isPmValueChanged());
    assertEquals(3, valueChangeListener.getEventCount());
    assertEquals(2, changedStateChangeListener.getEventCount());
    assertEquals(5, allEventsListener.getEventCount());
	}

}
