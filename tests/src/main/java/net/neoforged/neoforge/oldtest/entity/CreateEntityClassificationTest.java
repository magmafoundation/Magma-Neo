/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity;

import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.Mod;

@Mod("create_entity_classification_test")
public class CreateEntityClassificationTest {
    public static MobCategory test = MobCategory.valueOf("NEOTESTS_TEST");
}
