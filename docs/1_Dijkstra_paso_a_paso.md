# Taller Final de Complejidad Algorítmica
## Parte 1 — Algoritmo de Dijkstra paso a paso sobre el grafo del campus

> Documento pensado para copiarse **a mano**. Las tablas son pequeñas a propósito (6 nodos).

---

## 1. El grafo

**Nodos (V = 6)**

| Abreviatura | Lugar |
|---|---|
| M401 | Salón M401 (inicio y fin) |
| BIB | Biblioteca |
| CJ | Consultorio Jurídico |
| BJ | Bloque J |
| APF | Auditorio Papa Francisco |
| AIN | Auditorio Innovamater |

**Aristas:** grafo **no dirigido** y **completo** (15 aristas = C(6,2)). Cada peso es la distancia
caminada **directa** entre los dos lugares, medida en el campus (metros).

### Matriz de adyacencia / pesos (metros)

Es simétrica: `w(u,v) = w(v,u)`. La diagonal es 0.

|  | M401 | BIB | CJ | BJ | APF | AIN |
|---|---:|---:|---:|---:|---:|---:|
| **M401** | 0 | 260 | 441 | 288 | 140 | 250 |
| **BIB** | 260 | 0 | 305 | 80 | 150 | 175 |
| **CJ** | 441 | 305 | 0 | 307 | 365 | 172 |
| **BJ** | 288 | 80 | 307 | 0 | 284 | 160 |
| **APF** | 140 | 150 | 365 | 284 | 0 | 260 |
| **AIN** | 250 | 175 | 172 | 160 | 260 | 0 |

Todos los pesos son **no negativos** (> 0 fuera de la diagonal), que es la condición que exige Dijkstra.

---

## 2. El algoritmo (versión con matriz, O(V²))

```
Dijkstra(W, s):
    para cada nodo v:  d[v] ← ∞ ;  pred[v] ← ninguno ;  fijo[v] ← falso
    d[s] ← 0
    repetir V veces:
        u ← nodo NO fijo con menor d[u]          (si d[u] = ∞, parar)
        fijo[u] ← verdadero
        para cada nodo v NO fijo:
            si d[u] + W[u][v] < d[v]:            (relajación)
                d[v]    ← d[u] + W[u][v]
                pred[v] ← u
    devolver d, pred
```

Cada iteración: **(a)** se escoge el nodo no fijo más cercano, **(b)** se fija, **(c)** se relajan sus
aristas hacia los nodos no fijos.

---

## 3. Dijkstra desde M401 (origen = punto de partida)

Notación: `d = [M401, BIB, CJ, BJ, APF, AIN]`.

**Inicialización:** `d = [0, ∞, ∞, ∞, ∞, ∞]`, ningún nodo fijo.

| Iter | Nodo que se fija | d tras relajar `[M401, BIB, CJ, BJ, APF, AIN]` | Relajaciones que mejoraron |
|---:|---|---|---|
| 1 | **M401** (0) | `[0, 260, 441, 288, 140, 250]` | BIB ∞→260; CJ ∞→441; BJ ∞→288; APF ∞→140; AIN ∞→250 |
| 2 | **APF** (140) | `[0, 260, 441, 288, 140, 250]` | ninguna (ver cuentas abajo) |
| 3 | **AIN** (250) | `[0, 260, 422, 288, 140, 250]` | **CJ 441→422** |
| 4 | **BIB** (260) | `[0, 260, 422, 288, 140, 250]` | ninguna |
| 5 | **BJ** (288) | `[0, 260, 422, 288, 140, 250]` | ninguna |
| 6 | **CJ** (422) | `[0, 260, 422, 288, 140, 250]` | ninguna |

**Cuentas de cada relajación** (`d[u] + W[u][v]` contra `d[v]` actual):

- **Iter 2, u = APF (140):** BIB 140+150 = 290 ≥ 260 ✗; CJ 140+365 = 505 ≥ 441 ✗; BJ 140+284 = 424 ≥ 288 ✗; AIN 140+260 = 400 ≥ 250 ✗.
- **Iter 3, u = AIN (250):** BIB 250+175 = 425 ≥ 260 ✗; **CJ 250+172 = 422 < 441 ✓**; BJ 250+160 = 410 ≥ 288 ✗.
- **Iter 4, u = BIB (260):** CJ 260+305 = 565 ≥ 422 ✗; BJ 260+80 = 340 ≥ 288 ✗.
- **Iter 5, u = BJ (288):** CJ 288+307 = 595 ≥ 422 ✗.
- **Iter 6, u = CJ (422):** no quedan nodos sin fijar.

**Resultado desde M401**

| Destino | Distancia mínima | Camino (por `pred`) |
|---|---:|---|
| BIB | 260 | M401 → BIB |
| CJ | **422** | M401 → AIN → CJ *(mejor que la arista directa de 441)* |
| BJ | 288 | M401 → BJ |
| APF | 140 | M401 → APF |
| AIN | 250 | M401 → AIN |

---

## 4. Dijkstra desde BJ (el otro caso donde el camino directo no es el mínimo)

**Inicialización:** `d = [∞, ∞, ∞, 0, ∞, ∞]` (orden `[M401, BIB, CJ, BJ, APF, AIN]`).

| Iter | Nodo que se fija | d tras relajar `[M401, BIB, CJ, BJ, APF, AIN]` | Relajaciones que mejoraron |
|---:|---|---|---|
| 1 | **BJ** (0) | `[288, 80, 307, 0, 284, 160]` | M401 ∞→288; BIB ∞→80; CJ ∞→307; APF ∞→284; AIN ∞→160 |
| 2 | **BIB** (80) | `[288, 80, 307, 0, 230, 160]` | **APF 284→230** (80+150) |
| 3 | **AIN** (160) | `[288, 80, 307, 0, 230, 160]` | ninguna |
| 4 | **APF** (230) | `[288, 80, 307, 0, 230, 160]` | ninguna |
| 5 | **M401** (288) | `[288, 80, 307, 0, 230, 160]` | ninguna |
| 6 | **CJ** (307) | `[288, 80, 307, 0, 230, 160]` | ninguna |

Resultado: **BJ → APF = 230** pasando por BIB (BJ → BIB → APF), no la arista directa de 284.

---

## 5. Matriz de distancias mínimas (Dijkstra desde cada nodo)

Se corre Dijkstra con cada nodo como origen. Solo **dos** pares cambian respecto de la matriz de pesos:

| Par | Arista directa | Distancia mínima | Camino |
|---|---:|---:|---|
| M401 – CJ | 441 | **422** | M401 → AIN → CJ |
| BJ – APF | 284 | **230** | BJ → BIB → APF |

Matriz final `D` (distancias mínimas, metros):

|  | M401 | BIB | CJ | BJ | APF | AIN |
|---|---:|---:|---:|---:|---:|---:|
| **M401** | 0 | 260 | **422** | 288 | 140 | 250 |
| **BIB** | 260 | 0 | 305 | 80 | 150 | 175 |
| **CJ** | **422** | 305 | 0 | 307 | 365 | 172 |
| **BJ** | 288 | 80 | 307 | 0 | **230** | 160 |
| **APF** | 140 | 150 | 365 | **230** | 0 | 260 |
| **AIN** | 250 | 175 | 172 | 160 | 260 | 0 |

---

## 6. Ruta óptima del recorrido completo (M401 → los 5 puntos → M401)

Dijkstra da la distancia entre **dos** puntos. La ruta óptima del juego es el orden de visita de
los 5 puntos que minimiza la suma de tramos usando la matriz `D`. Se resuelve con programación
dinámica sobre subconjuntos (la misma recursión que usa la app):

```
f(S, j) = mínimo recorrido que sale de M401, visita todos los puntos de S y termina en j ∈ S
f({j}, j) = D[M401][j]
f(S, j)   = min sobre i ∈ S\{j} de  f(S\{j}, i) + D[i][j]
Óptimo    = min sobre j de  f(todos, j) + D[j][M401]
```

**Tamaño 1** (solo ese punto)

| S | f(S, j) |
|---|---|
| {BIB} | BIB = 260 |
| {CJ} | CJ = 422 |
| {BJ} | BJ = 288 |
| {APF} | APF = 140 |
| {AIN} | AIN = 250 |

**Tamaño 2** (se anota entre paréntesis el nodo anterior)

| S | f(S, j) |
|---|---|
| {BIB, CJ} | BIB = 727 (←CJ); CJ = 565 (←BIB) |
| {BIB, BJ} | BIB = 368 (←BJ); BJ = 340 (←BIB) |
| {BIB, APF} | BIB = 290 (←APF); APF = 410 (←BIB) |
| {BIB, AIN} | BIB = 425 (←AIN); AIN = 435 (←BIB) |
| {CJ, BJ} | CJ = 595 (←BJ); BJ = 729 (←CJ) |
| {CJ, APF} | CJ = 505 (←APF); APF = 787 (←CJ) |
| {CJ, AIN} | CJ = 422 (←AIN); AIN = 594 (←CJ) |
| {BJ, APF} | BJ = 370 (←APF); APF = 518 (←BJ) |
| {BJ, AIN} | BJ = 410 (←AIN); AIN = 448 (←BJ) |
| {APF, AIN} | APF = 510 (←AIN); AIN = 400 (←APF) |

**Tamaño 3**

| S | f(S, j) |
|---|---|
| {BIB, CJ, BJ} | BIB = 809 (←BJ); CJ = 647 (←BJ); BJ = 807 (←BIB) |
| {BIB, CJ, APF} | BIB = 810 (←CJ); CJ = 595 (←BIB); APF = 877 (←BIB) |
| {BIB, CJ, AIN} | BIB = 727 (←CJ); CJ = 607 (←AIN); AIN = 737 (←CJ) |
| {BIB, BJ, APF} | BIB = 450 (←BJ); BJ = 370 (←BIB); APF = 518 (←BIB) |
| {BIB, BJ, AIN} | BIB = 490 (←BJ); BJ = 505 (←BIB); AIN = 500 (←BJ) |
| {BIB, APF, AIN} | BIB = 575 (←AIN); APF = 575 (←BIB); AIN = 465 (←BIB) |
| {CJ, BJ, APF} | CJ = 677 (←BJ); BJ = 812 (←CJ); APF = 959 (←BJ) |
| {CJ, BJ, AIN} | CJ = 620 (←AIN); BJ = 729 (←CJ); AIN = 767 (←CJ) |
| {CJ, APF, AIN} | CJ = 572 (←AIN); APF = 787 (←CJ); AIN = 677 (←CJ) |
| {BJ, APF, AIN} | BJ = 560 (←AIN); APF = 640 (←BJ); AIN = 530 (←BJ) |

**Tamaño 4**

| S | f(S, j) |
|---|---|
| {BIB, CJ, BJ, APF} | BIB = 892 (←BJ); CJ = 677 (←BJ); BJ = 890 (←BIB); APF = 959 (←BIB) |
| {BIB, CJ, BJ, AIN} | BIB = 809 (←BJ); CJ = 672 (←AIN); BJ = 807 (←BIB); AIN = 819 (←CJ) |
| {BIB, CJ, APF, AIN} | BIB = 852 (←AIN); CJ = 637 (←AIN); APF = 877 (←BIB); AIN = 767 (←CJ) |
| {BIB, BJ, APF, AIN} | BIB = 640 (←BJ); BJ = 625 (←AIN); APF = 640 (←BIB); AIN = 530 (←BJ) |
| {CJ, BJ, APF, AIN} | CJ = 702 (←AIN); BJ = 837 (←AIN); APF = 959 (←BJ); AIN = 849 (←CJ) |

**Tamaño 5 (todos los puntos) y regreso a M401**

| Termina en j | f(todos, j) | + D[j][M401] | Total |
|---|---:|---:|---:|
| BIB | 917 | 260 | 1177 |
| CJ | 702 | 422 | 1124 |
| BJ | 927 | 288 | 1215 |
| **APF** | 959 | 140 | **1099** |
| **AIN** | 849 | 250 | **1099** |

### Resultado

La distancia mínima del recorrido completo es **1099 m**, y hay **dos rutas óptimas empatadas** (una es la
inversa de la otra, porque el grafo es no dirigido):

```
Ruta A:  M401 → AIN → CJ → BJ → BIB → APF → M401
         250 + 172 + 307 + 80 + 150 + 140 = 1099 m

Ruta B:  M401 → APF → BIB → BJ → CJ → AIN → M401
         140 + 150 + 80 + 307 + 172 + 250 = 1099 m
```

Reconstrucción de la ruta A siguiendo los "←": APF ←BIB ←BJ ←CJ ←AIN ←M401.

Comprobación por fuerza bruta: se evaluaron las 5! = 120 permutaciones; el mínimo es 1099 m y el peor orden
(M401 → CJ → BIB → AIN → APF → BJ → M401) es 1680 m.

> En la app, ante un empate se elige el primer lugar en el orden de la lista
> (M401, BIB, CJ, BJ, APF, AIN), por lo que la ruta con respuestas correctas empieza por **APF** (Ruta B).

---

## 7. Complejidad

- Dijkstra con matriz de adyacencia: **O(V²)** por origen; para todos los pares: **O(V³)** = 6³ = 216 pasos.
- Orden óptimo del recorrido (programación dinámica sobre subconjuntos de los 5 puntos): **O(2ⁿ · n²)** con n = 5, es decir 32 · 25 = 800 operaciones, frente a 5! = 120 permutaciones completas por fuerza bruta (con n pequeño ambos son instantáneos).
