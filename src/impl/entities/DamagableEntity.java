package impl.entities;

/**
     DamagableEntity - сущность, которая может получать урон.
 */

public interface DamagableEntity {
    /**
     * 
     * @param amount количество урона, наносимого сущности.
     */
    public void damage(double amount);
}