# Terminalidad y consideraciones lógicas

## 1. Reglas del juego

- M401 es el inicio y el fin. Hay 5 puntos con tag NFC; en cada uno se responde una pregunta.
- El recorrido empieza con una pregunta en M401: su respuesta decide el primer destino. En total se responden 6 preguntas.
- Sea R el conjunto de puntos aún no visitados (al inicio, |R| = 5) y D la matriz de distancias mínimas de Dijkstra.
- Tras cada respuesta (incluida la de M401) se recalcula el camino desde el punto actual con los puntos de R:
  - **Correcta:** el siguiente destino es el punto de R que deja el recorrido restante hasta M401 más **corto**.
  - **Incorrecta:** el siguiente destino es el punto de R que deja el recorrido restante hasta M401 más **largo**.
- Si R queda vacío, el destino es M401. Si solo queda un punto, no hay otra opción y se va a él.
- La app nunca le dice al jugador si su respuesta fue correcta o incorrecta, ni si va por la ruta óptima: tras cada respuesta solo muestra el siguiente destino. Los resultados (aciertos, distancia caminada y distancia óptima) se muestran al terminar en M401.
- En la pantalla inicial los 5 lugares se listan en orden aleatorio, y ese mismo orden se usa en la lista de pendientes durante el recorrido, para que el orden mostrado no revele la ruta óptima.

## 2. Demostración de que el recorrido siempre termina

1. **El grafo es conexo y finito:** las 15 aristas existen con peso finito, así que D[a][b] es finita para todo par. Siempre hay camino entre dos lugares.
2. **Pregunta inicial:** la pregunta de M401 se responde una sola vez, antes de salir, y no modifica R; solo escoge el primer destino dentro de R.
3. **Función de progreso:** φ = |R|. Después de la pregunta inicial, cada respuesta se acepta una sola vez y solo para el destino actual, que pasa de R a "visitados". El siguiente destino se escoge dentro de R (o es M401 si R está vacío). Una respuesta correcta o incorrecta solo cambia *cuál* elemento de R se escoge; nunca devuelve un punto a R.
4. Por tanto φ baja exactamente 1 con cada respuesta en un punto: 5 → 4 → 3 → 2 → 1 → 0. Un entero no negativo que decrece estrictamente no puede decrecer indefinidamente, así que hay como máximo 5 respuestas en los puntos, más la de M401: 6 en total.
5. Con φ = 0 el único movimiento es el tramo hacia M401, de longitud finita (hecho 1). Al llegar, el recorrido termina.

Conclusión: para cualquier secuencia de respuestas, correctas o incorrectas, el recorrido termina en M401 después de visitar los 5 puntos, con a lo sumo 6 tramos. ∎

## 3. Consideraciones lógicas implementadas

- La arista directa no siempre es el mejor camino (M401–CJ: 441 → 422 vía AIN; BJ–APF: 284 → 230 vía BIB), por eso se usa D y no la matriz cruda.
- Con todas las respuestas correctas (las 6, incluida la de M401) se camina exactamente la ruta óptima: 1099 m.
- Una respuesta incorrecta en M401 ya penaliza: el primer destino pasa a ser el que deja el recorrido completo más largo.
- Una respuesta incorrecta nunca acorta la ruta; solo puede alargarla.
- Si queda un solo punto no hay ruta más larga posible, así que la respuesta incorrecta ya no penaliza.
- Un tag distinto del destino actual, ya visitado o desconocido no hace avanzar el recorrido, y una pregunta no se puede responder dos veces.
- Si dos destinos dejan el mismo costo restante, se elige siempre el primero de la lista M401, BIB, CJ, BJ, APF, AIN, para que el comportamiento sea reproducible.
