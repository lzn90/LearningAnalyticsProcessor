/**
 * Copyright 2013 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.apereo.lap.services.input.csv;

import java.io.File;

import org.apereo.lap.services.ConfigurationService;
import org.springframework.jdbc.core.JdbcTemplate;

public class SampleCourseCSVInputHandler extends CourseCSVInputHandler {

    public SampleCourseCSVInputHandler(ConfigurationService configuration, JdbcTemplate jdbcTemplate) {
        super(configuration, jdbcTemplate);
    }

    @Override
    public File getFile() {
    	return new File(this.config.inputDirectory, getFileName());
    }
}
