package jp.mkserver.bloom.bloomingrpgcore.api;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import com.shampaggon.crackshot.events.WeaponFireRateEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

import static org.bukkit.Bukkit.getServer;

/**
 * @author Mr_IK
 * @version 0.1-alpha
 */
public class CrackShotAPI {

    static CSDirector director;
    static{
        director = JavaPlugin.getPlugin(CSDirector.class);
    }

    /**
     * fireメソッド
     * CrackShotの銃を強制的に撃つ。
     * @param player プレイヤー変数
     * @param parentNode 銃のID
     * @param leftClick 左クリックか否か(関係なさそう)
     */
    public static void fire(Player player,String parentNode,boolean leftClick){
        WeaponPrepareShootEvent prepareEvent = new WeaponPrepareShootEvent(player, parentNode);
        getServer().getPluginManager().callEvent(prepareEvent);
        if (prepareEvent.isCancelled()) return;
        int gunSlot = player.getInventory().getHeldItemSlot();
        int shootDelay = director.getInt(parentNode + ".Shooting.Delay_Between_Shots");

        final int projAmount = director.getInt(parentNode + ".Shooting.Projectile_Amount");
        final boolean ammoEnable = director.getBoolean(parentNode + ".Ammo.Enable");
        final boolean oneTime = director.getBoolean(parentNode + ".Extras.One_Time_Use");
        String deviceType = director.getString(parentNode + ".Explosive_Devices.Device_Type");
        final String proType = director.getString(parentNode + ".Shooting.Projectile_Type");

        ItemStack item = getWeaponFromName(parentNode);
        final boolean isFullyAuto = director.getBoolean(parentNode + ".Fully_Automatic.Enable");
        int fireRate = director.getInt(parentNode + ".Fully_Automatic.Fire_Rate");
        boolean burstEnable = director.getBoolean(parentNode + ".Burstfire.Enable");
        int burstShots = director.getInt(parentNode + ".Burstfire.Shots_Per_Burst");
        int burstDelay = director.getInt(parentNode + ".Burstfire.Delay_Between_Shots_In_Burst");
        boolean meleeMode = director.getBoolean(parentNode + ".Item_Information.Melee_Mode");
        boolean shootDisable = director.getBoolean(parentNode + ".Shooting.Disable");
        final boolean reloadOn = director.getBoolean(parentNode + ".Reload.Enable");
        final boolean dualWield = director.isDualWield(player, parentNode, item);
        if (shootDisable || meleeMode)
            return;
        Vector shiftVector = director.determinePosition(player, dualWield, leftClick);
        final Location projLoc = player.getEyeLocation().toVector().add(shiftVector.multiply(0.2D)).toLocation(player.getWorld());


        final String actType = director.getString(parentNode + ".Firearm_Action.Type");
        final boolean tweakyAction = (actType != null && (actType.toLowerCase().contains("bolt") || actType.toLowerCase().contains("lever") || actType.toLowerCase().contains("pump")));

        if (oneTime && ammoEnable) {
            player.sendMessage(director.heading + "For '" + parentNode + "' - the 'One_Time_Use' node is incompatible with weapons using the Ammo module.");

            return;
        }
        if (proType != null && (proType.equalsIgnoreCase("grenade") || proType.equalsIgnoreCase("flare")) && projAmount == 0) {
            player.sendMessage(director.heading + "The weapon '" + parentNode + "' is missing a value for 'Projectile_Amount'.");

            return;
        }
        if (isFullyAuto) {
            if (burstEnable) {
                player.sendMessage(director.heading + "The weapon '" + parentNode + "' is using Fully_Automatic and Burstfire at the same time. Pick one; you cannot enable both!"); return;
            }
            if (shootDelay > 1) {
                player.sendMessage(director.heading + "For '" + parentNode + "' - the Fully_Automatic module can only be used if 'Delay_Between_Shots' is removed or set to a value no greater than 1."); return;
            }
            if (fireRate <= 0 || fireRate > 16) {
                player.sendMessage(director.heading + "The weapon '" + parentNode + "' has an invalid value for 'Fire_Rate'. The accepted values are 1 to 16.");

                return;
            }
        }
        if (director.itemIsSafe(item) && item.getItemMeta().getDisplayName().contains("ᴿ")) {
            if (director.getAmmoBetweenBrackets(player, parentNode, item) > 0) {
                if (!dualWield) {

                    director.terminateReload(player);


                    director.removeInertReloadTag(player, 0, true);
                } else {

                    int[] ammoReading = director.grabDualAmmo(item, parentNode);
                    if ((ammoReading[0] > 0 && leftClick) || (ammoReading[1] > 0 && !leftClick)) {
                        director.terminateReload(player);
                        director.removeInertReloadTag(player, 0, true);
                    }
                }
            } else {

                director.reloadAnimation(player, parentNode);

                return;
            }
        }

        if (!tweakyAction && (actType == null || !actType.equalsIgnoreCase("slide") || !item.getItemMeta().getDisplayName().contains("▫"))) {
            player.setMetadata(parentNode + "shootDelay" + gunSlot + leftClick, new FixedMetadataValue(director, Boolean.valueOf(true)));
            director.csminion.tempVars(player, parentNode + "shootDelay" + gunSlot + leftClick, Long.valueOf(shootDelay));
        }


        final String ammoInfo = director.getString(parentNode + ".Ammo.Ammo_Item_ID");
        final boolean ammoPerShot = director.getBoolean(parentNode + ".Ammo.Take_Ammo_Per_Shot");
        final double zoomAcc = director.getDouble(parentNode + ".Scope.Zoom_Bullet_Spread");
        final boolean sneakOn = director.getBoolean(parentNode + ".Sneak.Enable");
        boolean sneakToShoot = director.getBoolean(parentNode + ".Sneak.Sneak_Before_Shooting");
        final boolean sneakNoRec = director.getBoolean(parentNode + ".Sneak.No_Recoil");
        final double sneakAcc = director.getDouble(parentNode + ".Sneak.Bullet_Spread");
        final boolean exploDevs = director.getBoolean(parentNode + ".Explosive_Devices.Enable");
        boolean takeAmmo = director.getBoolean(parentNode + ".Reload.Take_Ammo_On_Reload");


        String dragRemInfo = director.getString(parentNode + ".Shooting.Removal_Or_Drag_Delay");
        final String[] dragRem = (dragRemInfo == null) ? null : dragRemInfo.split("-");
        if (dragRem != null) {
            try {
                Integer.valueOf(dragRem[0]);
            } catch (NumberFormatException ex) {
                player.sendMessage(director.heading + "For the weapon '" + parentNode + "', the 'Removal_Or_Drag_Delay' node is incorrectly configured.");

                return;
            }
        }

        if (director.getBoolean(parentNode + ".Ammo.Take_Ammo_On_Reload")) {
            player.sendMessage(director.heading + "For the weapon '" + parentNode + "', the Ammo module does not support the 'Take_Ammo_On_Reload' node. Did you mean to place it in the Reload module?");

            return;
        }

        if (ammoEnable) {
            if (!takeAmmo && !ammoPerShot) {
                player.sendMessage(director.heading + "The weapon '" + parentNode + "' has enabled the Ammo module, but at least one of the following nodes need to be set to true: Take_Ammo_On_Reload, Take_Ammo_Per_Shot."); return;
            }
            if (!director.csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {


                boolean isPumpOrBolt = (actType != null && !actType.equalsIgnoreCase("pump") && !actType.equalsIgnoreCase("bolt"));
                boolean hasLoadedChamber = item.getItemMeta().getDisplayName().contains("▪ «");

                if (ammoPerShot || (takeAmmo && director.getAmmoBetweenBrackets(player, parentNode, item) == 0 && (isPumpOrBolt || !hasLoadedChamber))) {
                    director.playSoundEffects(player, parentNode, ".Ammo.Sounds_Shoot_With_No_Ammo", false, null);

                    return;
                }
            }
        }

        if (sneakToShoot && (!player.isSneaking() || player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)) {
            return;
        }
        if (director.checkBoltPosition(player, parentNode)) {
            return;
        }
        if (!burstEnable) burstShots = 1;


        if (isFullyAuto) {
            burstShots = 5;
            burstDelay = 1;
        }


        final double projSpeed = director.getInt(parentNode + ".Shooting.Projectile_Speed") * 0.1D;


        final boolean setOnFire = director.getBoolean(parentNode + ".Shooting.Projectile_Flames");


        final boolean noBulletDrop = director.getBoolean(parentNode + ".Shooting.Remove_Bullet_Drop");


        if (director.getBoolean(parentNode + ".Scope.Zoom_Before_Shooting") && !player.hasMetadata("ironsights")) {
            return;
        }
        int shootReloadBuffer = director.getInt(parentNode + ".Reload.Shoot_Reload_Buffer");
        if (shootReloadBuffer > 0) {
            Map<Integer, Long> lastShot = director.last_shot_list.get(player.getName());
            if (lastShot == null) {
                lastShot = new HashMap<Integer, Long>();
                director.last_shot_list.put(player.getName(), lastShot);
            }
            lastShot.put(Integer.valueOf(gunSlot), Long.valueOf(System.currentTimeMillis()));
        }


        int burstStart = 0;
        if (isFullyAuto) {


            WeaponFireRateEvent event = new WeaponFireRateEvent(player, parentNode, item, fireRate);
            getServer().getPluginManager().callEvent(event);
            fireRate = event.getFireRate();


            String playerName = player.getName();
            if (!director.rpm_ticks.containsKey(playerName)) {
                director.rpm_ticks.put(playerName, Integer.valueOf(1));
            }


            if (!director.rpm_shots.containsKey(playerName)) {
                director.rpm_shots.put(playerName, Integer.valueOf(0));
            }


            burstStart = director.rpm_shots.get(playerName).intValue();


            director.rpm_shots.put(playerName, Integer.valueOf(5));
        }


        final int fireRateFinal = fireRate;


        final int itemSlot = player.getInventory().getHeldItemSlot();

        for (int burst = burstStart; burst < burstShots; burst++) {
            final boolean isLastShot = (burst >= burstShots - 1);

            int task_ID = Bukkit.getScheduler().scheduleSyncDelayedTask(director, () -> {
                if (isFullyAuto) {


                    String playerName = player.getName();
                    int shotsLeft = director.rpm_shots.get(playerName).intValue() - 1;
                    director.rpm_shots.put(playerName, Integer.valueOf(shotsLeft));


                    int tick = director.rpm_ticks.get(playerName).intValue();
                    director.rpm_ticks.put(playerName, Integer.valueOf((tick >= 20) ? 1 : (tick + 1)));


                    if (shotsLeft == 0) {
                        director.burst_task_IDs.remove(playerName);
                    }


                    if (!director.isValid(tick, fireRateFinal))
                    {
                        return;

                    }

                } else if (isLastShot) {
                    director.burst_task_IDs.remove(player.getName());
                }

                ItemStack item1 = getWeaponFromName(parentNode);


                if (!oneTime) {
                    if (switchedTheItem(item, parentNode) || itemSlot != player.getInventory().getHeldItemSlot()) {
                        director.unscopePlayer(player);
                        director.terminateAllBursts(player);
                        return;
                    }
                    boolean normalAction = false;
                    if (actType == null) {
                        normalAction = true;
                        String attachType = director.getAttachment(parentNode, item1)[0];
                        String filter = item1.getItemMeta().getDisplayName();
                        if (attachType == null || !attachType.equalsIgnoreCase("accessory")) {
                            if (filter.contains("▪ «")) {
                                director.csminion.setItemName(item1, filter.replaceAll("▪ «", "«"));
                            } else if (filter.contains("▫ «")) {
                                director.csminion.setItemName(item1, filter.replaceAll("▪ «", "«"));
                            } else if (filter.contains("۔ «")) {
                                director.csminion.setItemName(item1, filter.replaceAll("۔ «", "«"));
                            }
                        }
                    } else if (!tweakyAction) {
                        normalAction = true;
                    }

                    if (ammoEnable && ammoPerShot && !director.csminion.containsItemStack(player, ammoInfo, 1, parentNode)) {
                        director.burst_task_IDs.remove(player.getName());
                        return;
                    }
                    if (reloadOn) {
                        if (item1.getItemMeta().getDisplayName().contains("ᴿ"))
                            return;  int detectedAmmo = director.getAmmoBetweenBrackets(player, parentNode, item1);

                        if (normalAction) {
                            if (detectedAmmo > 0) {
                                if (!dualWield) {
                                    director.ammoOperation(player, parentNode, detectedAmmo, item1);
                                } else if (!director.ammoSpecOps(player, parentNode, detectedAmmo, item1, leftClick)) {
                                    return;
                                }
                            } else {
                                director.reloadAnimation(player, parentNode);
                                return;
                            }
                        }
                    } else {
                        String itemName = item1.getItemMeta().getDisplayName();
                        if (itemName.contains("«") && !itemName.contains(String.valueOf('×')) && !exploDevs) {
                            director.csminion.replaceBrackets(item1, String.valueOf('×'), parentNode);
                        }
                    }
                }

                double bulletSpread = director.getDouble(parentNode + ".Shooting.Bullet_Spread");
                if (player.isSneaking() && sneakOn) bulletSpread = sneakAcc;
                if (player.hasMetadata("ironsights")) bulletSpread = zoomAcc;
                if (bulletSpread == 0.0D) bulletSpread = 0.1D;


                boolean noVertRecoil = director.getBoolean(parentNode + ".Abilities.No_Vertical_Recoil");
                boolean jetPack = director.getBoolean(parentNode + ".Abilities.Jetpack_Mode");
                double recoilAmount = director.getInt(parentNode + ".Shooting.Recoil_Amount") * 0.1D;
                if (recoilAmount != 0.0D && (!sneakOn || !sneakNoRec || !player.isSneaking())) {
                    if (!jetPack) {
                        Vector velToAdd = player.getLocation().getDirection().multiply(-recoilAmount);
                        if (noVertRecoil) velToAdd.multiply(new Vector(1,0,1));
                        player.setVelocity(velToAdd);
                    } else {

                        player.setVelocity(new Vector(0.0D, recoilAmount, 0.0D));
                    }
                }


                boolean clearFall = director.getBoolean(parentNode + ".Shooting.Reset_Fall_Distance");
                if (clearFall) player.setFallDistance(0.0F);


                director.csminion.giveParticleEffects(player, parentNode, ".Particles.Particle_Player_Shoot", true, null);


                director.csminion.givePotionEffects(player, parentNode, ".Potion_Effects.Potion_Effect_Shooter", "shoot");


                director.csminion.displayFireworks(player, parentNode, ".Fireworks.Firework_Player_Shoot");


                director.csminion.runCommand(player, parentNode);


                if (director.getBoolean(parentNode + ".Abilities.Hurt_Effect")) {
                    player.playEffect(EntityEffect.HURT);
                }


                String projectile_type = director.getString(parentNode + ".Shooting.Projectile_Type");



                int timer = director.getInt(parentNode + ".Explosions.Explosion_Delay");
                boolean airstrike = director.getBoolean(parentNode + ".Airstrikes.Enable");
                if (airstrike) timer = director.getInt(parentNode + ".Airstrikes.Flare_Activation_Delay");


                String soundsShoot = director.getString(parentNode + ".Shooting.Sounds_Shoot");
                WeaponPreShootEvent event = new WeaponPreShootEvent(player, parentNode, soundsShoot, bulletSpread, leftClick);
                director.plugin.getServer().getPluginManager().callEvent(event);


                director.playSoundEffects(player, parentNode, null, false, null, event.getSounds());


                if (event.isCancelled())
                    return;  bulletSpread = event.getBulletSpread();


                for (int i = 0; i < projAmount; i++) {
                    Random r = new Random();
                    double yaw = Math.toRadians((-player.getLocation().getYaw() - 90.0F));
                    double pitch = Math.toRadians(-player.getLocation().getPitch());
                    double[] spread = { 1.0D, 1.0D, 1.0D };
                    for (int t = 0; t < 3; ) { spread[t] = (r.nextDouble() - r.nextDouble()) * bulletSpread * 0.1D; t++; }
                    double x = Math.cos(pitch) * Math.cos(yaw) + spread[0];
                    double y = Math.sin(pitch) + spread[1];
                    double z = -Math.sin(yaw) * Math.cos(pitch) + spread[2];
                    Vector dirVel = new Vector(x, y, z);

                    if (proType != null && (proType.equalsIgnoreCase("grenade") || proType.equalsIgnoreCase("flare"))) {
                        director.launchGrenade(player, parentNode, timer, dirVel.multiply(projSpeed), null, 0);
                    } else if (proType.equalsIgnoreCase("energy")) {
                        double radius; int hitLimit, range; PermissionAttachment attachment = player.addAttachment(director.plugin);
                        attachment.setPermission("nocheatplus", true);
                        attachment.setPermission("anticheat.check.exempt", true);


                        String proOre = director.getString(parentNode + ".Shooting.Projectile_Subtype");
                        if (proOre == null) {
                            player.sendMessage(director.heading + "The weapon '" + parentNode + "' does not have a value for 'Projectile_Subtype'.");
                            return;
                        }
                        String[] proInfo = proOre.split("-");
                        if (proInfo.length != 4) {
                            player.sendMessage(director.heading + "The value provided for 'Projectile_Subtype' of the weapon '" + parentNode + "' has an incorrect format.");


                            return;
                        }


                        int wallLimit = 0;

                        int hitCount = 0;
                        int wallCount = 0;
                        try {
                            range = Integer.valueOf(proInfo[0]).intValue();
                            hitLimit = Integer.valueOf(proInfo[3]).intValue();
                            if (proInfo[2].equalsIgnoreCase("all")) {
                                wallLimit = -1;
                            } else if (!proInfo[2].equalsIgnoreCase("none")) {
                                wallLimit = Integer.valueOf(proInfo[2]).intValue();
                            }
                            radius = Double.valueOf(proInfo[1]).doubleValue();
                        } catch (NumberFormatException ex) {
                            player.sendMessage(director.heading + "The value provided for 'Projectile_Subtype' of the weapon '" + parentNode + "' contains an invalid number.");

                            break;
                        }
                        Set<Block> hitBlocks = new HashSet<Block>();
                        Set<Integer> hitMobs = new HashSet<Integer>();
                        Vector vecShift = dirVel.normalize().multiply(radius);
                        Location locStart = player.getEyeLocation();

                        double k;
                        label198: for (k = 0.0D; k < range; k += radius) {
                            locStart.add(vecShift);
                            Block hitBlock = locStart.getBlock();

                            if (hitBlock.getType() == Material.AIR) {


                                FallingBlock tempEnt = player.getWorld().spawnFallingBlock(locStart, Material.AIR, (byte)0);

                                for (Entity ent : tempEnt.getNearbyEntities(radius, radius, radius)) {
                                    if (ent instanceof LivingEntity && ent != player && !hitMobs.contains(Integer.valueOf(ent.getEntityId())) && !ent.isDead()) {
                                        if (ent instanceof Player) {
                                            ent.setMetadata("CS_Energy", new FixedMetadataValue(director.plugin, parentNode));
                                            ((LivingEntity)ent).damage(0.0D, player);
                                        } else {
                                            director.dealDamage(player, (LivingEntity)ent, null, parentNode);
                                        }

                                        hitMobs.add(Integer.valueOf(ent.getEntityId()));
                                        hitCount++;


                                        if (hitLimit != 0 && hitCount >= hitLimit) {
                                            break label198;
                                        }
                                    }
                                }


                                tempEnt.remove();
                            }
                            else if (wallLimit != -1 && !hitBlocks.contains(hitBlock)) {


                                wallCount++;
                                if (wallCount > wallLimit) {
                                    break;
                                }
                                hitBlocks.add(hitBlock);
                            }
                        }


                        director.callShootEvent(player, null, parentNode);
                        director.playSoundEffects(player, parentNode, ".Shooting.Sounds_Projectile", false, null);

                        player.removeAttachment(attachment);
                    } else if (proType.equalsIgnoreCase("splash")) {
                        ThrownPotion splashPot = player.getWorld().spawn(projLoc, ThrownPotion.class);
                        ItemStack potType = director.csminion.parseItemStack(director.getString(parentNode + ".Shooting.Projectile_Subtype"));
                        if (potType != null) {
                            try {
                                splashPot.setItem(potType);
                            } catch (IllegalArgumentException ex) {
                                player.sendMessage(director.heading + "The value for 'Projectile_Subtype' of weapon '" + parentNode + "' is not a splash potion!");
                            }
                        }
                        if (setOnFire) splashPot.setFireTicks(6000);
                        if (noBulletDrop) director.noArcInArchery(splashPot, dirVel.multiply(projSpeed));
                        splashPot.setShooter(player);
                        splashPot.setMetadata("projParentNode", new FixedMetadataValue(director.plugin, parentNode));
                        splashPot.setVelocity(dirVel.multiply(projSpeed));
                        director.callShootEvent(player, splashPot, parentNode);
                        if (dragRem != null) director.prepareTermination(splashPot, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
                    } else {
                        Projectile snowball; if (projectile_type.equalsIgnoreCase("arrow")) {
                            snowball = (Projectile)player.getWorld().spawnEntity(projLoc, EntityType.ARROW);
                        } else if (projectile_type.equalsIgnoreCase("egg")) {
                            snowball = (Projectile)player.getWorld().spawnEntity(projLoc, EntityType.EGG);
                            snowball.setMetadata("CS_Hardboiled", new FixedMetadataValue(director.plugin, Boolean.valueOf(true)));
                        } else if (projectile_type.equalsIgnoreCase("fireball")) {
                            snowball = player.launchProjectile(LargeFireball.class);


                            if (Boolean.parseBoolean(director.getString(parentNode + ".Shooting.Projectile_Subtype"))) {
                                snowball.setMetadata("CS_NoDeflect", new FixedMetadataValue(director.plugin, Boolean.valueOf(true)));
                            }
                        } else if (projectile_type.equalsIgnoreCase("witherskull")) {
                            snowball = player.launchProjectile(WitherSkull.class);
                        } else {
                            snowball = (Projectile)player.getWorld().spawnEntity(projLoc, EntityType.SNOWBALL);
                        }

                        if (setOnFire) snowball.setFireTicks(6000);
                        if (noBulletDrop) director.noArcInArchery(snowball, dirVel.multiply(projSpeed));
                        snowball.setShooter(player);
                        snowball.setVelocity(dirVel.multiply(projSpeed));
                        snowball.setMetadata("projParentNode", new FixedMetadataValue(director.plugin, parentNode));
                        director.callShootEvent(player, snowball, parentNode);
                        director.playSoundEffects(snowball, parentNode, ".Shooting.Sounds_Projectile", false, null);
                        if (dragRem != null) director.prepareTermination(snowball, Boolean.parseBoolean(dragRem[1]), Long.valueOf(dragRem[0]));
                    }
                }
            },Long.valueOf((burstDelay * burst)).longValue() + 1L);


            if (oneTime && burst == 0 && (deviceType == null || (!deviceType.equalsIgnoreCase("remote") && !deviceType.equalsIgnoreCase("trap")))) director.csminion.oneTime(player);


            String user = player.getName();
            Collection<Integer> values = director.burst_task_IDs.get(user);
            if (values == null) {
                values = new ArrayList<Integer>();
                director.burst_task_IDs.put(user, values);
            }
            values.add(Integer.valueOf(task_ID));
        }
    }


    public static boolean switchedTheItem( ItemStack item, String parent_node) {
        String attachType = director.getAttachment(parent_node, item)[0];
        boolean attachment = attachType != null && attachType.equalsIgnoreCase("accessory");
        return item == null || !director.itemIsSafe(item) || !attachment && director.isDifferentItem(item, parent_node);
    }

    public static ItemStack getWeaponFromName(String parent_node){
        String attachType = director.getString(parent_node + ".Item_Information.Attachments.Type");
        if (attachType == null || !attachType.equalsIgnoreCase("accessory")) {
            return new CSUtility().generateWeapon(parent_node);
        }else{
            for(String weapons:director.wlist.values()){
                String attachType2 = director.getString(weapons + ".Item_Information.Attachments.Type");
                if (attachType2 != null && attachType2.equalsIgnoreCase("main")&&director.getString(weapons + ".Item_Information.Attachments.Info").equalsIgnoreCase(parent_node)) {
                    ItemStack item =  new CSUtility().generateWeapon(weapons);
                    String itemName = item.getItemMeta().getDisplayName();
                    String triOne = String.valueOf('▶');
                    String triTwo = String.valueOf('▷');
                    String triThree = String.valueOf('◀');
                    String triFour = String.valueOf('◁');
                    if (itemName.contains(triThree)) {
                        director.csminion.setItemName(item, itemName.replaceAll(triThree + triTwo, triFour + triOne));
                    } else {
                        director.csminion.setItemName(item, itemName.replaceAll(triFour + triOne, triThree + triTwo));
                    }
                    return item;
                }
            }
        }
        return null;
    }

    public static boolean isNameWeaponContain(String parent_node){
        return getWeaponFromName(parent_node)!=null;
    }
}
