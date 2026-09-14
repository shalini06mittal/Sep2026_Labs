# ArenaClash — Solutions to All Mini-Tasks

This file gives one working solution per stage. Each builds on the previous, so by Stage 10 you have the full, compilable `ArenaClash` codebase. Your own solution doesn't need to match exactly — if it compiles and behaves the same, it's correct.

---

## Stage 1 Solution — Data Types and Operators

```java
public class Main {
    public static void main(String[] args) {
        String gamertag = "ShadowByte";
        int level = 42;
        double health = 100.0;
        double maxHealth = 100.0;
        boolean isAlive = true;
        final int MAX_LEVEL = 100;
        char rankTier = 'D';

        int killCount = 12;
        double damageDealt = 3120.0;

        // cast to double BEFORE dividing, or int/int truncates to 0
        double kdRatio = damageDealt / (double) killCount;

        double healthPercent = health / maxHealth * 100;

        System.out.println(gamertag + " | Lv." + level + " | " + healthPercent + "% HP | Rank: " + rankTier);
        System.out.println("Alive? " + isAlive);
        System.out.println("Kills: " + killCount + " | Damage: " + damageDealt + " | KD-per-damage: " + kdRatio);
    }
}
```

**Why the cast matters:** `damageDealt / killCount` — if `damageDealt` were an `int` too, this would floor to a whole number, silently destroying the stat. Casting one operand to `double` forces Java to do floating-point division.

---

## Stage 2 Solution — Conditional Constructs

```java
int roll = 87; // simulate a loot roll 1-100

String rarity;
if (roll <= 50) {
    rarity = "Common";
} else if (roll <= 80) {
    rarity = "Rare";
} else if (roll <= 95) {
    rarity = "Epic";
} else {
    rarity = "Legendary";
}
System.out.println("if/else rarity: " + rarity);

// switch version: bucket the roll into a tier number first, then switch
int tier;
if (roll <= 50) tier = 1;
else if (roll <= 80) tier = 2;
else if (roll <= 95) tier = 3;
else tier = 4;

String rarityBySwitch = switch (tier) {
    case 1 -> "Common";
    case 2 -> "Rare";
    case 3 -> "Epic";
    case 4 -> "Legendary";
    default -> "Unknown";
};
System.out.println("switch rarity: " + rarityBySwitch);
```

**Note:** Java's `switch` matches exact values, not ranges — that's why the roll is bucketed into a `tier` int first. (Newer Java versions support pattern-matching `switch` with guards like `case Integer i when i <= 50 ->`, but the bucket approach is simpler and very common.)

---

## Stage 3 Solution — Loops

```java
int rounds = 6;
double total = 0;
double highestRound = Double.MIN_VALUE;
int roundsOver100 = 0;

for (int round = 1; round <= rounds; round++) {
    double roundDamage = round * 20 + 10;   // given formula, no array needed
    total += roundDamage;

    if (roundDamage > highestRound) {
        highestRound = roundDamage;
    }
    if (roundDamage > 100) {
        roundsOver100++;
    }

    System.out.println("Round " + round + ": " + roundDamage + " dmg");
}

System.out.println("Total damage: " + total);
System.out.println("Highest round: " + highestRound);
System.out.println("Rounds over 100 dmg: " + roundsOver100);
```

**Why `Double.MIN_VALUE` as the starting "highest"?** It's smaller than any real damage value, so the very first round is guaranteed to beat it and set a real baseline — avoids hardcoding a starting guess like `0` that might accidentally be wrong for some formulas (e.g., all-negative values, though that won't happen here).

---

## Stage 4 Solution — Arrays Basics

```java
double[] matchDamage = {245.5, 89.0, 512.3, 0.0, 178.0};

double highest = matchDamage[0];
double total = 0;
int zeroDamageGames = 0;

for (double dmg : matchDamage) {   // for-each: values only, no index needed here
    total += dmg;
    if (dmg > highest) {
        highest = dmg;
    }
    if (dmg == 0.0) {
        zeroDamageGames++;
    }
}

double average = total / matchDamage.length;

System.out.println("Highest single-game damage: " + highest);
System.out.println("Average damage: " + average);
System.out.println("Zero-damage games: " + zeroDamageGames);

// squad health part
int[] squadHealth = {85, 42, 100, 15, 60};

int healthTotal = 0;
boolean anyoneNeedsRevive = false;

for (int hp : squadHealth) {
    healthTotal += hp;
    if (hp < 20) {
        anyoneNeedsRevive = true;
    }
}

double squadAverageHealth = healthTotal / (double) squadHealth.length;

System.out.println("Squad average HP: " + squadAverageHealth);
System.out.println("Someone needs a revive? " + anyoneNeedsRevive);
```

**Why one loop instead of three separate ones for `matchDamage`?** Accumulating `total`, `highest`, and `zeroDamageGames` in a single pass is both idiomatic and more efficient — no need for three trips through the array.

**Why `(double) squadHealth.length`?** Same integer-division trap as Stage 1 — `healthTotal / squadHealth.length` with two `int`s truncates the average to a whole number.

---

## Stage 5 Solution — Methods

```java
public class Main {

    static double totalDamage(double[] rounds) {
        double sum = 0;
        for (double d : rounds) {
            sum += d;
        }
        return sum;
    }

    static double averageHealth(int[] squadHealth) {
        int sum = 0;
        for (int hp : squadHealth) {
            sum += hp;
        }
        return sum / (double) squadHealth.length;
    }

    static boolean needsRevive(int health) {
        return health < 20;
    }

    public static void main(String[] args) {
        double[] matchDamage = {245.5, 89.0, 512.3, 0.0, 178.0};
        int[] squadHealth = {85, 42, 100, 15, 60};

        System.out.println("Total damage: " + totalDamage(matchDamage));
        System.out.println("Squad average HP: " + averageHealth(squadHealth));

        for (int hp : squadHealth) {
            System.out.println(hp + " HP -> needs revive? " + needsRevive(hp));
        }
    }
}
```

**Why extract these into methods now?** Once `Character` objects exist (Stage 6+), you'll call these same calculations against real game data instead of raw arrays — having them as standalone, testable methods makes that transition painless.

---

## Stage 6 Solution — Classes, Objects, Constructors, Getters/Setters, toString, Encapsulation

```java
// Character.java
public class Character {
    private String name;
    private int level;
    private double health;
    private double maxHealth;
    private final String characterClass;

    public Character(String name, int level, double maxHealth, String characterClass) {
        this.name = name;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.characterClass = characterClass;
    }

    public double getHealth() {
        return health;
    }

    public String getName() {
        return name;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage can't be negative");
        }
        health -= amount;
        if (health < 0) health = 0;
    }

    public void heal(double amount) {
        health = Math.min(health + amount, maxHealth);
    }

    @Override
    public String toString() {
        return name + " (Lv." + level + " " + characterClass + ") — " + health + "/" + maxHealth + " HP";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Character)) return false;
        Character other = (Character) obj;
        return this.name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode(); // required whenever you override equals()
    }
}
```

```java
// Main.java
public class Main {
    public static void main(String[] args) {
        Character c1 = new Character("ShadowByte", 42, 100, "Rogue");
        Character c2 = new Character("TankZilla", 38, 150, "Brute");
        Character c3 = new Character("HealQueen", 30, 90, "Support");

        c1.takeDamage(35);
        c2.heal(10); // no-op, already at max
        c3.takeDamage(200); // overkill -> clamps to 0

        System.out.println(c1);
        System.out.println(c2);
        System.out.println(c3);
        System.out.println("c3 alive? " + c3.isAlive());

        Character c1Again = new Character("ShadowByte", 1, 1, "Whatever");
        System.out.println("c1 equals c1Again (same name)? " + c1.equals(c1Again));
    }
}
```

**Why override `hashCode()` alongside `equals()`?** Java's rule: two objects that are `.equals()` must return the same `hashCode()`. If you skip this, the `Character` will misbehave inside `HashMap`/`HashSet` (Stage 9) — e.g., looking it up by an "equal" key might fail. Always override both together.

---

## Stage 7 Solution — Inheritance and Polymorphism

```java
// Mage.java
public class Mage extends Character {
    private double mana;

    public Mage(String name, int level, double maxHealth, double mana) {
        super(name, level, maxHealth, "Mage");
        this.mana = mana;
    }

    public void castFireball() {
        if (mana < 20) {
            System.out.println(getName() + " is out of mana!");
            return;
        }
        mana -= 20;
        System.out.println(getName() + " casts Fireball! 🔥");
    }

    @Override
    public String toString() {
        return super.toString() + " | Mana: " + mana;
    }
}
```

```java
// Warrior.java
public class Warrior extends Character {
    private double armor;

    public Warrior(String name, int level, double maxHealth, double armor) {
        super(name, level, maxHealth, "Warrior");
        this.armor = armor;
    }

    @Override
    public void takeDamage(double amount) {
        double reduced = amount * (1 - armor);
        super.takeDamage(reduced);
    }
}
```

```java
// Healer.java
public class Healer extends Character {
    public Healer(String name, int level, double maxHealth) {
        super(name, level, maxHealth, "Healer");
    }

    public void healAlly(Character ally, double amount) {
        ally.heal(amount);
        System.out.println(getName() + " heals " + ally.getName() + " for " + amount + " HP");
    }
}
```

```java
// Main.java
public class Main {
    public static void main(String[] args) {
        Character[] squad = {
            new Mage("ShadowByte", 42, 100, 80),
            new Warrior("TankZilla", 38, 150, 0.3),
            new Healer("HealQueen", 30, 90)
        };

        // round: everyone takes 30 damage
        for (Character c : squad) {
            c.takeDamage(30);
        }

        for (Character c : squad) {
            System.out.println(c);
        }

        // find lowest-HP teammate and have the Healer heal them
        Character lowest = squad[0];
        for (Character c : squad) {
            if (c.getHealth() < lowest.getHealth()) {
                lowest = c;
            }
        }

        for (Character c : squad) {
            if (c instanceof Healer healer) {   // pattern-matching instanceof
                healer.healAlly(lowest, 20);
            }
        }

        System.out.println("\nAfter healing:");
        for (Character c : squad) {
            System.out.println(c);
        }
    }
}
```

**Why does `Warrior`'s `takeDamage` reduce damage but still call `super.takeDamage()`?** This is the standard "override, but reuse the parent's core logic" pattern — `Warrior` only needs to change *how much* damage gets applied, not the clamping-to-zero/negative-check logic that already lives correctly in `Character`. Re-implementing that logic in `Warrior` would duplicate code and risk drifting out of sync.

---

## Stage 8 Solution — Interfaces

```java
// Ultimate.java
public interface Ultimate {
    void activateUltimate();

    default String ultimateReadyMessage() {
        return "Ultimate ready! Press Q!";
    }
}
```

```java
// Mage.java (updated)
public class Mage extends Character implements Ultimate {
    private double mana;

    public Mage(String name, int level, double maxHealth, double mana) {
        super(name, level, maxHealth, "Mage");
        this.mana = mana;
    }

    public void castFireball() {
        if (mana < 20) {
            System.out.println(getName() + " is out of mana!");
            return;
        }
        mana -= 20;
        System.out.println(getName() + " casts Fireball! 🔥");
    }

    @Override
    public void activateUltimate() {
        System.out.println(getName() + " unleashes METEOR STORM! ☄️☄️☄️");
    }

    @Override
    public String toString() {
        return super.toString() + " | Mana: " + mana;
    }
}
```

```java
// Warrior.java (updated)
public class Warrior extends Character implements Ultimate {
    private double armor;

    public Warrior(String name, int level, double maxHealth, double armor) {
        super(name, level, maxHealth, "Warrior");
        this.armor = armor;
    }

    @Override
    public void takeDamage(double amount) {
        super.takeDamage(amount * (1 - armor));
    }

    @Override
    public void activateUltimate() {
        System.out.println(getName() + " activates UNSTOPPABLE RAGE! 💥");
    }
}
```

```java
// Main.java (relevant part)
public class Main {

    static void triggerAllUltimates(Character[] squad) {
        for (Character c : squad) {
            if (c instanceof Ultimate u) {   // only characters that implement Ultimate
                System.out.println(u.ultimateReadyMessage());
                u.activateUltimate();
            }
        }
    }

    public static void main(String[] args) {
        Character[] squad = {
            new Mage("ShadowByte", 42, 100, 80),
            new Warrior("TankZilla", 38, 150, 0.3),
            new Healer("HealQueen", 30, 90)   // no ultimate -- skipped safely
        };

        triggerAllUltimates(squad);
    }
}
```

**Why `instanceof Ultimate`, not `instanceof Mage`?** Checking against the interface means `triggerAllUltimates` works for *any* current or future class that implements `Ultimate` — you never need to touch this method again if you add a new `Assassin` class with its own ultimate later.

---

## Stage 9 Solution — ArrayList and Map

```java
// Squad.java
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Squad {
    private List<Character> members = new ArrayList<>();
    private Map<String, Integer> xpByPlayer = new HashMap<>();

    public void addMember(Character c) {
        members.add(c);
        xpByPlayer.put(c.getName(), 0); // start every player at 0 XP
    }

    public void addXp(String playerName, int amount) {
        int current = xpByPlayer.getOrDefault(playerName, 0);
        xpByPlayer.put(playerName, current + amount);
    }

    public double totalSquadHealth() {
        double total = 0;
        for (Character c : members) {
            total += c.getHealth();
        }
        return total;
    }

    public String topPlayer() {
        String topName = null;
        int topXp = -1;
        for (Map.Entry<String, Integer> entry : xpByPlayer.entrySet()) {
            if (entry.getValue() > topXp) {
                topXp = entry.getValue();
                topName = entry.getKey();
            }
        }
        return topName + " (" + topXp + " XP)";
    }

    public List<Character> getMembers() {
        return members;
    }
}
```

```java
// Main.java (relevant part)
public class Main {
    public static void main(String[] args) {
        Squad squad = new Squad();
        squad.addMember(new Mage("ShadowByte", 42, 100, 80));
        squad.addMember(new Warrior("TankZilla", 38, 150, 0.3));
        squad.addMember(new Healer("HealQueen", 30, 90));

        squad.addXp("ShadowByte", 1500);
        squad.addXp("TankZilla", 900);
        squad.addXp("HealQueen", 1200);
        squad.addXp("ShadowByte", 500); // more XP after another match

        System.out.println("Total squad HP: " + squad.totalSquadHealth());
        System.out.println("Top player: " + squad.topPlayer());
    }
}
```

**Why `getOrDefault(playerName, 0)` instead of `xpByPlayer.get(playerName)`?** If the player isn't in the map yet, plain `.get()` returns `null`, and `null + amount` throws a `NullPointerException`. `getOrDefault` sidesteps that entirely — a very common, idiomatic Java pattern for "increment a counter in a map."

---

## Stage 10 Solution — Exception Handling (Final Integration)

```java
// OutOfManaException.java
public class OutOfManaException extends Exception {
    public OutOfManaException(String message) {
        super(message);
    }
}
```

```java
// TargetEliminatedException.java
public class TargetEliminatedException extends Exception {
    public TargetEliminatedException(String message) {
        super(message);
    }
}
```

```java
// Character.java (updated takeDamage)
public void takeDamage(double amount) throws TargetEliminatedException {
    if (amount < 0) {
        throw new IllegalArgumentException("Damage can't be negative"); // unchecked: programmer error
    }
    if (!isAlive()) {
        throw new TargetEliminatedException(name + " is already eliminated!"); // checked: expected game state
    }
    health -= amount;
    if (health < 0) health = 0;
}
```

> Note: since `takeDamage` now declares `throws TargetEliminatedException`, every overriding method (like `Warrior.takeDamage`) and every caller must also handle or re-declare it. Update `Warrior`:

```java
// Warrior.java (updated)
@Override
public void takeDamage(double amount) throws TargetEliminatedException {
    super.takeDamage(amount * (1 - armor));
}
```

```java
// Mage.java (updated castFireball)
public void castFireball() throws OutOfManaException {
    if (mana < 20) {
        throw new OutOfManaException(getName() + " doesn't have enough mana!");
    }
    mana -= 20;
    System.out.println(getName() + " casts Fireball! 🔥");
}
```

```java
// Squad.java (add the attack method)
public void attack(Character attacker, Character target, double damage) {
    try {
        target.takeDamage(damage);
        System.out.println(attacker.getName() + " hits " + target.getName() + " for " + damage);
    } catch (TargetEliminatedException e) {
        System.out.println("Attack failed: " + e.getMessage());
    } finally {
        System.out.println(attacker.getName() + "'s turn ends.");
    }
}
```

```java
// Main.java (final match simulation)
public class Main {
    public static void main(String[] args) {
        Squad squad = new Squad();
        Mage mage = new Mage("ShadowByte", 42, 100, 15); // low mana on purpose
        Warrior warrior = new Warrior("TankZilla", 38, 150, 0.3);
        Healer healer = new Healer("HealQueen", 30, 90);

        squad.addMember(mage);
        squad.addMember(warrior);
        squad.addMember(healer);

        squad.addXp("ShadowByte", 1500);
        squad.addXp("TankZilla", 900);
        squad.addXp("HealQueen", 1200);

        // mage tries an ultimate spell with insufficient mana
        try {
            mage.castFireball();
        } catch (OutOfManaException e) {
            System.out.println("Spell failed: " + e.getMessage());
        }

        // combat round
        squad.attack(warrior, mage, 200);       // eliminates the mage (200 > 100 hp)
        squad.attack(warrior, mage, 10);         // attacking an eliminated target -> caught gracefully

        // ultimates for whoever still qualifies
        Character[] squadArray = squad.getMembers().toArray(new Character[0]);
        for (Character c : squadArray) {
            if (c instanceof Ultimate u && c.isAlive()) {
                u.activateUltimate();
            }
        }

        // final match summary
        System.out.println("\n=== MATCH SUMMARY ===");
        System.out.println("Survivors:");
        for (Character c : squadArray) {
            if (c.isAlive()) {
                System.out.println("  " + c);
            }
        }
        System.out.println("Top player: " + squad.topPlayer());
    }
}
```

**Why does the second `attack()` call not crash the program?** Because `Squad.attack()` wraps the risky call in `try/catch`, a "target already eliminated" scenario — a totally normal thing to happen mid-match — gets logged and the match continues, instead of the whole program terminating. That's the entire point of checked exceptions in game logic: known, recoverable failure paths must be explicitly handled, not accidentally ignored.

---

## Full File List (final project structure)

```
Main.java
Character.java
Mage.java
Warrior.java
Healer.java
Ultimate.java
Squad.java
OutOfManaException.java
TargetEliminatedException.java
```

Compile and run the whole thing with:
```bash
javac *.java
java Main
```
