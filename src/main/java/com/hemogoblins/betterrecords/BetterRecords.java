/*
 * Copyright (C) 2021 Hemogoblins
 * This file is part of BetterRecords.
 *
 * BetterRecords is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BetterRecords is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BetterRecords.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.hemogoblins.betterrecords;

import com.hemogoblins.betterrecords.proxy.ClientProxy;
import com.hemogoblins.betterrecords.proxy.CommonProxy;
import com.hemogoblins.betterrecords.proxy.Proxy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BetterRecords.MOD_ID)
public class BetterRecords {

    public static final String MOD_ID = "betterrecords";

    public static Proxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public BetterRecords() {
        //noinspection deprecation
        DistExecutor.callWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
        //noinspection deprecation
        DistExecutor.callWhenOn(Dist.DEDICATED_SERVER, () -> () -> proxy = new CommonProxy());

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, com.hemogoblins.betterrecords.Config.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, com.hemogoblins.betterrecords.Config.COMMON_SPEC);

        proxy.onConstruct();
    }
}
