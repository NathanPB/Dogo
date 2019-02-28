package io.github.dogo.core.data

import ninja.leaping.configurate.objectmapping.Setting
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable

/*
Copyright 2019 Nathan Bombana

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
 * Tables configurations.
 * @see [DogoData]
 *
 * @author NathanPB
 * @since 3.1.0
 */
@ConfigSerializable
class Database {

    /**
     * The host.
     */
    @Setting
    var HOST = "localhost"

    /**
     * The database name.
     */
    @Setting
    var NAME = "Dogo"

    /**
     * The database user.
     */
    @Setting
    var USER = "root"

    /**
     * The user password.
     */
    @Setting
    var PWD = "root"

    /**
     * The port.
     */
    var PORT = 3306

}