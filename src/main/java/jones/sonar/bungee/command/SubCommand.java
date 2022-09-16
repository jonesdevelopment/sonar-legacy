/*
 *  Copyright (c) 2022, jones (https://jonesdev.xyz) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jones.sonar.bungee.command;

public abstract class SubCommand {

    public final String name, description, permission;

    public final String[] aliases;

    public abstract void execute(final CommandExecution execution);

    /*
     * This class needs custom constructors because we have
     * a command alias which needs to be defined and no alias
     * at the same time.
     */

    public SubCommand(final String name, final String description, final String permission, final String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.permission = permission;
    }

    public SubCommand(final String name, final String description, final String permission) {
        this.name = name;
        this.description = description;
        this.aliases = new String[] { name };
        this.permission = permission;
    }
}
