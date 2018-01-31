# Report
ComicCollecter is een app die stripverzamelaars helpt bij het sparen van Marvel comics. Je kan alle Marvel comics opslaan en hier een score aan toekennen. Op deze manier heb je je collectie overal bij de hand. 
*insert collection screen*
## Technical Design
comicCollector opent op de gebruiker haar collectie, dit is namelijk hetgene wat het belangrijkste is. je kan vervolgens door je collectie heen scrollen en informatie over comics vinden, door er op te klikken. 
Je kan andere strips ontdekken op de Browse-pagina. Hierop staan 50 random strips weergegeven, deze kan de gebruiker ook toevoegen aan de collectie. De gebruiker kan andere users vinden op de Search-pagina. als je deze opent staan de recente gebruikers, zodat je je favoriete gebruikers snel terug kan vinden. Tot slot kan de gebruiker uitloggen door op de Sign-out knop te drukken. Zie de video voor een gedetailleerde walkthrough.
### design
In DESIGN.md staat het originele ontwerp. Deze is totaal niet meer toepasbaar, ik heb heel veel aangepast. De structuur zit als volgt in elkaar:
- *mainActivity*
    - hierin worden alle fragments geladen en staan een groot aantal universele functies. Hierin is ook de bottomnavigation te vinden. 

    - *collectionView* 
        - is de class waarmee collectie grids worden weergegeven. hierin wordt bepaald welke collectie opgehaald moet worden en welke informatie moet worden doorgegeven aan de comicInfo fragment.
    - *browseComic* 
        -  is de class waarmee de browse pagina wordt gebouwd. Hierin wordt een offset bepaald, waarmee de comics opgehaald worden. Daarnaast zitten er dezelfde functionaliteiten van collectionView in .
    - *searchUsers*
        - is de class waarmee de gebruiker andere mensen op kan zoeken. Hierin worden uit de shared preferences de recent bekeken mensen opgehaald en in firebase gezocht naar gebruikers. 
    - *comicInfo*
        - is de class waarmee de gebruiker meer over een comic te weten komt. dit wordt ingeladen vanuit én FireBase én de Marvel API. In deze class staan veel functies die ook in andere classes worden gebruikt. Vanuit dit fragment is het mogelijk om je collectie aan te passen of toe te voegen. het toevoegen gaat door op het plusje te drukken óf als je al de comic bezit, de edit knop lang ingedrukt te houden. Je kan hem aanpassen met de edit knop.
        - *addComicDialog*
            - is de class waarmee je comics kan toevoegen aan je collectie. Dit doe je door de slider op het juiste aantal te zetten en de invulvelden in te vullen. als je deze invult en bevestigt, bekijkt de class of het aan de voorwaarden voldoet en slaat hem vervolgens op in FireBase. Je kan 5 scores per keer toevoegen.
- *authentication*  
    - Dit is de class waarin de fragments worden geladen om je aan te melden of in te loggen.
    - 
- 






