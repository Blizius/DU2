# Program pro interpolaci dat metodou IDW

## Funkce

Program určený pro interpolaci prostorových dat ze zadaných souřadnic x, y, z v .csv souboru. Interpolace se provádí na rovnoměrné mřížce 100x100, kde ve výsledném souboru budou v takovémto formátu zapsány (100 řádků o 100 sloupcích hodnot).

Při spuštění je potřeba uvést argumenty. Přepínač -d pokud chcete použít soubor, kde jsou všechny x-ové souřadnice na druhém řádku, y-ové na třetím a z-tové na čtvrtém. Jinak musíte použít soubor, kde každý řádek značí 3 souřadnice jednoho bodu (x,y,z). První řádek je vždy vymezen pro jedno celé číslo udávající počet bodů v souboru. Dále můžete uvést parametr -p <číslo> pro zvolení libovolné (kladné) mocniny vah pro výpočet IDW. Na předposlední místo parametru uveďte cestu ke vstupnímu souboru a na poslední místo cestu k výstupnímu souboru. Oba ve formátu .csv (možné i .txt). Po proběhnutí programu a otevření výstupního souboru v excelu budou všechny hodnoty v buňkách.

## Funkčnost

Všechny chybné možnosti ve vstupním souboru jsou ošetřeny a při jakékoli chybě se vypíše příslušné chybové hlášení a program se s chybou ukončí. Jsou zde též dvě možnosti, jak mohou být vstupní data formátována, avšak to je třeba určit přepínačem -d při spuštění. Volitelný parametr -p <číslo> lze také při spuštění nastavit a poté se budou váhy počítat se zadanou mocninou, která však nemůže být nula nebo záporná, jelikož by pak výpočet nedával smysl.

## Implementace

V hlavní funkci se načte pole Stringů args[], kde jsou všechny přepínače a parametry (především cesty k souborům) potřebné pro běh programu. Poté se po vybrání typu formátování dat (přepínač -d) zavolá příslušná funkce, kde se v cyklu zapíší souřadnice ze souboru do dovjrozměrného pole. Pokud se objeví nějaká výjimka, provede se příslušné chybové ukončení. Poté se zavolá funkce IDW, která na základě vstupních dat vytvoří souřadnice pro všechny interpolované body (mřížku 100x100) a v nich pomocí trojitého cyklu postupně vypočítá všechny z-ové hodnoty v interpolované mřížce. Pokud interpolovaný bod leží na vstupním bodu, jeho hodnota se pouze zkopíruje. Následně se vyskočí z funkce a výstupní pole z-ových souřadnic se v cyklu vypíše do výstupního souboru, tak aby hodnoty tvořili mřížku 100x100.
