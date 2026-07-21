# Organitia

Fork de [Fossify Calendar](https://github.com/FossifyOrg/Calendar) orienté planification quotidienne avec agenda, tâches hiérarchiques et alarmes précises.

## Fonctionnalités ajoutées

### Vue agenda depuis le mois
- Bouton **Vue agenda** sur la vue mensuelle pour passer à la vue mois + liste (agenda du jour sélectionné).
- Bouton **Voir le mois** dans la vue agenda pour revenir à la vue mensuelle pure.

### Vue journalière enrichie
- Affichage des **tâches** avec leurs **sous-tâches** indentées dans la vue du jour.
- Édition des sous-tâches dans l’écran de tâche (titre, complétion, alarme).

### Alarmes à la seconde près
- Rappels stockés et planifiés en **secondes** (HH:MM:SS).
- Heure des tâches affichée avec les secondes.
- Alarmes dédiées par sous-tâche à l’heure exacte (HH:MM:SS).

## Construction

```bash
./gradlew assembleFossDebug
```

## Crédits

Basé sur [Fossify Calendar](https://github.com/FossifyOrg/Calendar) (GPL-3.0).
