# Algoritmo de Dijkstra sobre el grafo del campus (a mano)

## 1. Grafo y matriz de pesos (metros)

Nodos: M401, BIB (Biblioteca), CJ (Consultorio Jurídico), BJ (Bloque J), APF (Auditorio Papa Francisco), AIN (Auditorio Innovamater).
Grafo no dirigido, pesos = distancia caminada directa entre lugares (matriz simétrica).

|  | M401 | BIB | CJ | BJ | APF | AIN |
|---|---:|---:|---:|---:|---:|---:|
| **M401** | 0 | 260 | 441 | 288 | 140 | 250 |
| **BIB** | 260 | 0 | 305 | 80 | 150 | 175 |
| **CJ** | 441 | 305 | 0 | 307 | 365 | 172 |
| **BJ** | 288 | 80 | 307 | 0 | 284 | 160 |
| **APF** | 140 | 150 | 365 | 284 | 0 | 260 |
| **AIN** | 250 | 175 | 172 | 160 | 260 | 0 |

## 2. Algoritmo

```
d[origen] = 0 ; d[v] = ∞ para los demás ; ningún nodo fijo
repetir 6 veces:
    u = nodo no fijo con menor d[u] ; fijar u
    para cada v no fijo:  si d[u] + w(u,v) < d[v]  →  d[v] = d[u] + w(u,v)
```

## 3. Ejecución desde M401

Orden de columnas: `[M401, BIB, CJ, BJ, APF, AIN]`

| Paso | Se fija | d después de relajar |
|---:|---|---|
| 1 | M401 (0) | `[0, 260, 441, 288, 140, 250]` |
| 2 | APF (140) | `[0, 260, 441, 288, 140, 250]` |
| 3 | AIN (250) | `[0, 260, 422, 288, 140, 250]` ← CJ mejora: 250 + 172 = 422 < 441 |
| 4 | BIB (260) | `[0, 260, 422, 288, 140, 250]` |
| 5 | BJ (288) | `[0, 260, 422, 288, 140, 250]` |
| 6 | CJ (422) | `[0, 260, 422, 288, 140, 250]` |

Distancias mínimas desde M401: BIB 260, CJ **422** (M401→AIN→CJ), BJ 288, APF 140, AIN 250.

Repitiendo el algoritmo con cada nodo como origen, solo otro par mejora: **BJ–APF = 230**
(BJ→BIB→APF = 80 + 150, mejor que la arista directa de 284). Todos los demás pares quedan igual.

## 4. Ruta óptima del recorrido (M401 → los 5 puntos → M401)

**Cómo se halló.** Dijkstra da la distancia mínima entre dos lugares. Para el recorrido completo hay que
decidir el orden de visita de los 5 puntos:

1. Un recorrido es un orden de los 5 puntos (el orden importa, y ningún punto se repite), con costo
   `D[M401][p1] + D[p1][p2] + D[p2][p3] + D[p3][p4] + D[p4][p5] + D[p5][M401]`.
2. Hay 5! = 120 órdenes. Como el grafo es no dirigido, recorrer un orden al revés cuesta lo mismo, así que
   solo hay que comparar **60 rutas distintas**.
3. Se calcula el costo de cada una con las distancias mínimas de Dijkstra y se escoge la menor.
   (La app lo hace igual, con la recursión *costo(actual, siguiente) = D + óptimo del resto*; las pruebas
   automáticas lo comprueban contra la enumeración completa.)

**Resultado:**

```
M401 → APF → BIB → BJ → CJ → AIN → M401
 140  + 150 + 80  + 307 + 172 + 250 = 1099 m
```

Su inverso (M401 → AIN → CJ → BJ → BIB → APF → M401) suma lo mismo, 1099 m. La siguiente mejor ruta,
M401 → APF → BIB → BJ → AIN → CJ → M401, suma 140 + 150 + 80 + 160 + 172 + 422 = 1124 m, y la peor,
M401 → CJ → BIB → AIN → APF → BJ → M401, suma 1680 m. Ninguna de las 60 rutas baja de 1099 m.
