/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.gwt.client.curricula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class CurriculumFilterBox extends UniTimeFilterBox<CurriculumInterface.CurriculumFilterRpcRequest> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private ListBox iDepartments;
	private FilterBox.CustomFilter iOther = null;
	
	public CurriculumFilterBox() {
		super(null);
		
		setShowSuggestionsOnFocus(false);

		iDepartments = new ListBox(false);
		iDepartments.setWidth("100%");
		
		addFilter(new FilterBox.CustomFilter("department", iDepartments) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					Chip oldChip = getChip("department");
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iDepartments.getItemCount(); i++) {
						Chip chip = new Chip("department", iDepartments.getValue(i));
						String name = iDepartments.getItemText(i);
						if (iDepartments.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						} else if (text.length() > 2 && (name.toLowerCase().contains(" " + text.toLowerCase()) || name.toLowerCase().contains(" (" + text.toLowerCase()))) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
		});
		iDepartments.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				Chip oldChip = getChip("department");
				Chip newChip = (iDepartments.getSelectedIndex() <= 0 ? null : new Chip("department", iDepartments.getValue(iDepartments.getSelectedIndex())));
				if (oldChip != null) {
					if (newChip == null) {
						removeChip(oldChip, true);
					} else {
						if (!oldChip.getValue().equals(newChip.getValue())) {
							removeChip(oldChip, false);
							addChip(newChip, true);
						}
					}
				} else {
					if (newChip != null)
						addChip(newChip, true);
				}
			}
		});
		
		addFilter(new FilterBox.StaticSimpleFilter("area"));
		addFilter(new FilterBox.StaticSimpleFilter("major"));
		addFilter(new FilterBox.StaticSimpleFilter("classification"));
		addFilter(new FilterBox.StaticSimpleFilter("curriculum"));
		
		final TextBox curriculum = new TextBox();
		curriculum.setStyleName("unitime-TextArea");
		curriculum.setMaxLength(100); curriculum.setWidth("200px");
		
		iOther = new FilterBox.CustomFilter("other", new Label(MESSAGES.propCurriculum()), curriculum);
		addFilter(iOther);
		
		curriculum.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("curriculum", null), false);
				if (curriculum.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("curriculum", curriculum.getText()), true);
				}
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!isFilterPopupShowing()) {
					iDepartments.setSelectedIndex(0);
					for (int i = 1; i < iDepartments.getItemCount(); i++) {
						String value = iDepartments.getValue(i);
						if (hasChip(new Chip("department", value))) {
							iDepartments.setSelectedIndex(i);
							break;
						}
					}
					Chip chip = getChip("curriculum");
					if (chip == null)
						curriculum.setText("");
					else
						curriculum.setText(chip.getValue());
				}
				init(false, getAcademicSessionId(), new Command() {
					@Override
					public void execute() {
						if (isFilterPopupShowing())
							showFilterPopup();
					}
				});
			}
		});
	}
	
	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if ("department".equals(filter.getCommand())) {
			iDepartments.clear();
			iDepartments.addItem(MESSAGES.itemAllDepartments(), "");
			if (entities != null)
				for (FilterRpcResponse.Entity entity: entities)
					iDepartments.addItem(entity.getName() + " (" + entity.getCount() + ")", entity.getAbbreviation());
			
			iDepartments.setSelectedIndex(0);
			Chip dept = getChip("department");
			if (dept != null)
				for (int i = 1; i < iDepartments.getItemCount(); i++)
					if (dept.getValue().equals(iDepartments.getValue(i))) {
						iDepartments.setSelectedIndex(i);
						break;
					}
			return true;
		} else if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<FilterBox.Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities)
					chips.add(new FilterBox.Chip(filter.getCommand(), entity.getAbbreviation(), entity.getName(), entity.getCount() <= 0 ? null : "(" + entity.getCount() + ")"));
			}
			simple.setValues(chips);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public CurriculumInterface.CurriculumFilterRpcRequest createRpcRequest() {
		return new CurriculumInterface.CurriculumFilterRpcRequest();
	}
}
