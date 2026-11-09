# Taller Final de Complejidad Algorítmica
## Parte 2 — Terminalidad (convergencia) y consideraciones lógicas

> Documento pensado para copiarse **a mano**. Los datos del grafo y la matriz de pesos están en
> `1_Dijkstra_paso_a_paso.md`.

---

## 1. Qué hace la app (modelo)

- 6 nodos: M401 (inicio/fin) y 5 puntos con tag NFC: BIB, CJ, BJ, APF, AIN.
- Estado del jugador: `visitados` (puntos ya respondidos) y `objetivo` (próximo destino).
- Al escanear el tag del objetivo se muestra una pregunta de 4 opciones.
- Después de **cada** respuesta se elige el siguiente destino entre los puntos **no visitados**:
  - **Correcta:** el siguiente destino es el que deja el recorrido restante más corto (ruta óptima).
  - **Incorrecta:** se **recalcula el camino** desde el lugar donde está el jugador, con los puntos que
    le faltan por visitar, y el siguiente destino es el que deja el recorrido restante **más largo**.
    No hay ningún desvío ni valor fijo: la penalización sale solo de la diferencia entre rutas.
- Cuando no quedan puntos sin visitar, el objetivo pasa a ser **M401** y el recorrido termina al llegar.

Notación: `R` = conjunto de puntos aún no visitados (|R| empieza en 5), `c` = punto actual,
`D` = matriz de distancias mínimas (Dijkstra, ver Parte 1).

Costo de elegir `n ∈ R` como siguiente destino:

```
costo(c, n, R) = D[c][n] + óptimo(n, R \ {n})
óptimo(c, ∅)   = D[c][M401]
óptimo(c, R)   = mínimo sobre n ∈ R de costo(c, n, R)
```

- Correcta → `n* = argmin costo(c, n, R)`.
- Incorrecta → `n⁻ = argmax costo(c, n, R)`; la penalización es `costo(n⁻) − costo(n*) ≥ 0`.
  Vale 0 solo cuando no existe una ruta más larga: queda un único punto, o todas las opciones cuestan igual.

**Ejemplo de recálculo.** El jugador va por la ruta óptima, llega a APF (ya caminó 140 m) y responde mal.
Quedan R = {BIB, CJ, BJ, AIN}. Se calcula el costo de terminar el recorrido según el siguiente destino:

| Siguiente | D[APF][n] | + mejor resto hasta M401 | Costo total restante |
|---|---:|---|---:|
| BIB | 150 | 809 (BIB→BJ→CJ→AIN→M401) | 959 ← **ruta más corta** (la correcta) |
| CJ | 365 | 672 (CJ→AIN→BJ→BIB→M401) | 1037 |
| BJ | 230 | 807 (BJ→BIB→CJ→AIN→M401) | 1037 |
| AIN | 260 | 819 (AIN→CJ→BJ→BIB→M401) | **1079** ← ruta más larga |

Con la respuesta incorrecta la app lo manda a **AIN** (260 m). Penalización = 1079 − 959 = **120 m**; si desde
ahí acierta todo, camina 140 + 1079 = 1219 m = 1099 + 120.

---

## 2. Demostración de terminalidad (convergencia)

**Afirmación.** Para **cualquier** secuencia de respuestas (correctas o incorrectas) el recorrido
termina, después de exactamente 5 respuestas y un tramo final hacia M401.

### 2.1 Hechos previos

1. **El grafo es conexo y finito.** Hay 6 nodos y las 15 aristas existen con peso finito
   (la matriz de pesos no tiene ∞ fuera de la diagonal). Por tanto Dijkstra devuelve `D[a][b] < ∞`
   para todo par: **siempre existe un camino** entre cualquier pareja de lugares.
2. **Los pesos son positivos**, así que cada tramo tiene longitud finita y > 0; Dijkstra es correcto
   (nunca fija un nodo con una distancia que luego pueda mejorarse).
3. **La decisión siempre está definida.** Mientras `R ≠ ∅` el conjunto de opciones no es vacío, de
   modo que `argmin` y `argmax` existen. Cuando `R = ∅` el destino es M401 (único y alcanzable por el hecho 1).

### 2.2 Función de progreso

Sea `φ = |R|`, el número de puntos sin visitar. Inicialmente `φ = 5`.

- Cada respuesta se acepta **una sola vez** y **solo** para el punto que es el objetivo actual
  (la app ignora tags de otros puntos, tags ya visitados y respuestas repetidas).
- Al aceptar la respuesta, el objetivo actual se **mueve de `R` a `visitados`**.
- El siguiente destino se escoge **dentro de `R`** (o es M401 si `R = ∅`). **Nunca** se escoge un
  punto ya visitado, ni en la rama correcta ni en la incorrecta (una respuesta incorrecta solo cambia *cuál*
  elemento de `R` se escoge, pero no devuelve elementos a `R`).

Por tanto, después de cada respuesta, `φ` **disminuye exactamente en 1**, sea correcta o incorrecta:

```
φ₀ = 5 > φ₁ = 4 > φ₂ = 3 > φ₃ = 2 > φ₄ = 1 > φ₅ = 0
```

`φ` es un entero no negativo que decrece estrictamente en cada paso ⇒ **no puede haber más de 5 respuestas**
(no existe una sucesión infinita estrictamente decreciente de enteros ≥ 0).

### 2.3 Cierre

Cuando `φ = 0` el único movimiento posible es el tramo `D[c][M401]`, finito por el hecho 1. Al llegar
a M401 el recorrido pasa a estado *terminado* (la app no permite terminar antes de visitar los 5 puntos).

**Por tanto** el recorrido tiene como máximo **6 tramos** (5 hacia puntos + 1 de regreso) y termina para
cualquier secuencia de respuestas. ∎

### 2.4 Cota de la distancia total (la penalización no rompe la terminación)

- Un tramo mide como máximo `max D = 422 m`, y hay a lo sumo 6 tramos.
- Por tanto: `distancia caminada ≤ 6 · 422 = 2532 m` (cota finita; la peor ruta posible mide 1680 m), y siempre se cumple

```
distancia caminada = recorrido óptimo + suma de penalizaciones = 1099 m + Σ penalizaciones
```

El mejor caso (5 respuestas correctas) camina exactamente **1099 m**. Una respuesta incorrecta suma una
penalización **mayor o igual a 0**: es positiva mientras exista una ruta más larga entre los puntos que quedan,
y 0 cuando ya no hay opción (por ejemplo, al responder mal el último punto, que solo deja volver a M401).

### 2.5 Verificación con pruebas automáticas

Las pruebas unitarias (`RoutingTest.kt`) recorren las **2⁵ = 32 secuencias posibles** de respuestas
correctas/incorrectas y comprueban, para cada una, que:

1. El recorrido termina en estado *terminado* con objetivo M401.
2. Los 5 puntos quedan visitados, sin repetir ninguno.
3. `distancia caminada = óptimo + penalizaciones`.
4. Tras una respuesta incorrecta, el siguiente destino es el de ruta más larga (comprobado por fuerza bruta
   desde el punto actual con los puntos pendientes) y la penalización nunca es negativa.
5. Con todas las respuestas correctas, la distancia es exactamente 1099 m.
6. El óptimo calculado coincide con la fuerza bruta sobre las 5! = 120 permutaciones.

---

## 3. Consideraciones lógicas implementadas

| Tema | Decisión |
|---|---|
| **Grafo** | No dirigido y completo (15 aristas medidas). Pesos = metros caminados reales. |
| **Camino más corto** | Dijkstra con matriz de adyacencia, O(V²) por origen. Se aplica desde cada nodo. |
| **Aristas "engañosas"** | La arista directa no siempre es el mejor camino: M401–CJ (441 → 422 vía AIN) y BJ–APF (284 → 230 vía BIB). Por eso se usa `D` y no la matriz cruda. |
| **Ruta óptima** | Orden de visita que minimiza la suma de tramos de `D` (1099 m). Hay dos óptimos empatados (inversos). Con empate, la app escoge el primero de la lista M401, BIB, CJ, BJ, APF, AIN. |
| **Respuesta correcta** | Siguiente destino = el de menor costo restante. |
| **Respuesta incorrecta** | Se recalcula desde el punto actual con los puntos sin visitar; siguiente destino = el de mayor costo restante (ruta más larga). Sin desvíos ni valores fijos. |
| **Garantía de finalización** | Cada respuesta elimina un punto de `R` (sección 2); el regreso a M401 siempre existe. |
| **Tags erróneos o repetidos** | Un tag distinto del objetivo, ya visitado o desconocido no avanza la ruta. |
| **Una sola respuesta por pregunta** | Una pregunta no se puede responder dos veces (evita cambiar el estado sin consumir un punto). |

### Casos límite que conviene mencionar

- **Últimos puntos con respuesta incorrecta:** si queda un solo punto (o ninguno, solo M401) no hay "ruta más
  larga" posible, porque no hay otra opción donde elegir. La penalización es 0 y el recorrido sigue terminando.
  Solo las respuestas incorrectas dadas con 2 o más puntos pendientes pueden alargar la ruta.
- **Empates:** si dos destinos dejan el mismo costo restante, la elección es determinista (primero en la
  lista), de modo que el comportamiento es reproducible.
- **Tramos que pasan físicamente por otro punto:** por ejemplo, el tramo M401 → CJ (422 m) pasa por la zona
  de Innovamater. Como solo se acepta el tag del objetivo actual, pasar cerca de otro cartel no cuenta
  como visita; el punto se visitará cuando sea su turno.
- **Distancias dependen de la medición:** si se vuelven a medir tramos, hay que recalcular `D`, el óptimo
  y los valores esperados en las pruebas (`dijkstraFindsPathsThroughOtherPlaces`, `optimalRouteIs1099Meters`).
