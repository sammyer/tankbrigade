package com.frozen.tankbrigade.map.moves;

/**
 * Created by sam on 14/12/14.
 */
public class DamageInfo {
	public static final DamageInfo NO_DAMAGE=new DamageInfo(0,0,0,0,false);
	public float damage;
	public float counterDamage;
	public float cost;
	public boolean isKill;

	public float attackCost;
	public float counterattackCost;

	public DamageInfo() {}

	public DamageInfo(float damage, float attackCost, float counterDamage, float counterattackCost, boolean isKill) {
		this.damage = damage;
		this.counterDamage = counterDamage;
		this.attackCost=attackCost;
		this.counterattackCost=counterattackCost;
		this.cost = attackCost-counterattackCost;
		this.isKill = isKill;
	}

	public boolean isNoDamage() {
		return (damage==0&& counterDamage ==0);
	}

	public String toString() {
		if (isNoDamage()) return "{Damage:None]";
		else return String.format("[Damage %.1f/%.1f cost=%.1f (%.1f/%.1f) kill=%b]",
				damage, counterDamage,cost,attackCost,counterattackCost,isKill);
	}
}
