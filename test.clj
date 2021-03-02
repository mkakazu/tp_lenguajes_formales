(require '[clojure.test :refer [is deftest run-tests]])

(load-file "basic.clj")

(deftest test-palabra-reservada?
   (is (= true (palabra-reservada? 'REM)))
   (is (= false (palabra-reservada? 'SPACE))))

(deftest test-operador?
  (is (= true (operador? '+)))
  (is (= true (operador? (symbol "+"))))
  (is (= false (operador? (symbol "%")))))

(deftest test-anular-invalidos
  (is (= (anular-invalidos '(IF X & * Y < 12 THEN LET ! X = 0))
         '(IF X nil * Y < 12 THEN LET nil X = 0))))

(deftest test-cargar-linea
  (def n0 '[((10 (PRINT X))) [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (def n1 '[((10 (PRINT X)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (def n2 '[((10 (PRINT X)) (15 (X = X + 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}])
  (def n3 '[((10 (PRINT X)) (15 (X = X - 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}])

  (is (= n0 (cargar-linea '(10 (PRINT X)) [() [:ejecucion-inmediata 0] [] [] [] 0 {}])))
  (is (= n1 (cargar-linea '(20 (X = 100)) ['((10 (PRINT X))) [:ejecucion-inmediata 0] [] [] [] 0 {}]) ))
  (is (= n2 (cargar-linea '(15 (X = X + 1)) ['((10 (PRINT X)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]) ))
  (is (= n3 (cargar-linea '(15 (X = X - 1)) ['((10 (PRINT X)) (15 (X = X + 1)) (20 (X = 100))) [:ejecucion-inmediata 0] [] [] [] 0 {}]) )))


(deftest test-expandir-nexts
  (def n0 (list '(PRINT 1) (list 'NEXT 'A (symbol ",") 'B)))
  (def n1 (list '(PRINT 1) (list 'NEXT 'A) (list 'NEXT 'B)))
  (def n2 (list '(PRINT 1) (list 'NEXT 'A)))
  (def n3 (list '(PRINT 1) (list 'NEXT)))
  (is (= (expandir-nexts n0) '((PRINT 1) (NEXT A) (NEXT B))))
  (is (= (expandir-nexts n1) '((PRINT 1) (NEXT A) (NEXT B))))
  (is (= (expandir-nexts n2) '((PRINT 1) (NEXT A))))
  (is (= (expandir-nexts n3) '((PRINT 1) (NEXT)))))

(deftest test-dar-error
 (is (= (dar-error 16 [:ejecucion-inmediata 4]) nil))
 (is (= (dar-error "?ERROR DISK FULL" [:ejecucion-inmediata 4]) nil))
 (is (= (dar-error 16 [100 3]) nil ))
 (is (= (dar-error "?ERROR DISK FULL" [100 3]) nil)))

(deftest test-variable-float?
  (is (= true (variable-float? 'X)))
  (is (= false (variable-float? 'X%)))
  (is (= false (variable-float? 'X$))))

(deftest test-variable-integer?
  (is (= true (variable-integer? 'X%)))
  (is (= false (variable-integer? 'X)))
  (is (= false (variable-integer? 'X$))))

(deftest test-variable-string?
  (is (= true (variable-string? 'X$)))
  (is (= false (variable-string? 'X)))
  (is (= false (variable-string? 'X%))))

(deftest test-contar-sentencias
  (def amb
    [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])
  (is (= 2 (contar-sentencias 10 amb)))
  (is (= 1 (contar-sentencias 15 amb)))
  (is (= 2 (contar-sentencias 20 amb))))

(deftest test-buscar-lineas-restantes
  (is (= (buscar-lineas-restantes [() [:ejecucion-inmediata 0] [] [] [] 0 {}]) nil))
  (is (= (buscar-lineas-restantes ['((PRINT X) (PRINT Y)) [:ejecucion-inmediata 2] [] [] [] 0 {}]) nil))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 2] [] [] [] 0 {}])
         (list (list 10 (list 'PRINT 'X) (list 'PRINT 'Y)) (list 15 (list 'X '= 'X '+ 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 1] [] [] [] 0 {}])
         (list (list 10 (list 'PRINT 'Y)) (list 15 (list 'X '= 'X '+ 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [10 0] [] [] [] 0 {}])
         (list (list 10) (list 15 (list 'X '= 'X '+ 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 1] [] [] [] 0 {}])
         (list (list 15 (list 'X '= 'X '+ 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 0] [] [] [] 0 {}])
         (list (list 15) (list 20 (list 'NEXT 'I (symbol ",") 'J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}])
         '((20 (NEXT I) (NEXT J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 2] [] [] [] 0 {}])
         '((20 (NEXT I) (NEXT J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 1] [] [] [] 0 {}])
         '((20 (NEXT J)))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 0] [] [] [] 0 {}])
         '((20))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 -1] [] [] [] 0 {}])
         '((20))))
  (is (= (buscar-lineas-restantes [(list '(10 (PRINT X) (PRINT Y)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [25 0] [] [] [] 0 {}])
         nil)))

(deftest test-continuar-linea
  (is (= (continuar-linea [(list '(10 (PRINT X)) '(15 (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}])
         [nil [(list (list 10 (list 'PRINT 'X)) (list 15 (list 'X '= 'X '+ 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [] [] [] 0 {}]]))
  (is (= (continuar-linea [(list '(10 (PRINT X)) '(15 (GOSUB 100) (X = X + 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [20 3] [[15 2]] [] [] 0 {}])
        [:omitir-restante [(list (list 10 (list 'PRINT 'X)) (list 15 (list 'GOSUB 100) (list 'X '= 'X '+ 1)) (list 20 (list 'NEXT 'I (symbol ",") 'J))) [15 1] [] [] [] 0 {}]])))

(deftest test-extraer-data
  (def n0 '((())))
  (def n1 (list '(10 (PRINT X) (REM ESTE NO) (DATA 30)) '(20 (DATA HOLA)) (list 100 (list 'DATA 'MUNDO (symbol ",") 10 (symbol ",") 20))))

  (is (= (extraer-data n0) '()))
  (is (= (extraer-data n1) '("HOLA" "MUNDO" 10 20))))

(deftest test-ejecutar-asignacion
  (is (= (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 {}])
       '[((10 (PRINT X))) [10 1] [] [] [] 0 {X 5}]))
  (is (= (ejecutar-asignacion '(X = 5) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}])
       '[((10 (PRINT X))) [10 1] [] [] [] 0 {X 5}]))
  (is (= (ejecutar-asignacion '(X = X + 1) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 2}])
       '[((10 (PRINT X))) [10 1] [] [] [] 0 {X 3}]))
  (is (= (ejecutar-asignacion '(X$ = X$ + " MUNDO") ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}])
         '[((10 (PRINT X))) [10 1] [] [] [] 0 {X$ "HOLA MUNDO"}])))

(deftest test-preprocesar-expresion
 (is (= (preprocesar-expresion '(X$ + " MUNDO" + Z$) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X$ "HOLA"}])
        '("HOLA" + " MUNDO" + "")))

 (is (= (preprocesar-expresion '(X + . / Y% * Z) ['((10 (PRINT X))) [10 1] [] [] [] 0 '{X 5 Y% 2}])
        (list 5 '+ 0 '/ 2 '* 0))))

(deftest test-desambiguar
  (def n1 (list '- 2 '* (symbol "(") '- 3 '+ 5 '- (symbol "(") '+ 2 '/ 7 (symbol ")") (symbol ")")))
  (def n2 (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ")")))
  (def n3 (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ",") 3 (symbol ")")))
  (def n4 (list 'MID$ (symbol "(") 1 (symbol ",") '- 2 '+ 'K (symbol ",") 3 (symbol ")")))

  (def t1 (list '-u 2 '* (symbol "(") '-u 3 '+ 5 '- (symbol "(") 2 '/ 7 (symbol ")") (symbol ")")))
  (def t2 (list 'MID$ (symbol "(") 1 (symbol ",") 2 (symbol ")")))
  (def t3 (list 'MID3$ (symbol "(") 1 (symbol ",") 2 (symbol ",") 3 (symbol ")")))
  (def t4 (list 'MID3$ (symbol "(") 1 (symbol ",") '-u 2 '+ 'K (symbol ",") 3 (symbol ")")))

  (is (= t1 (desambiguar n1)))
  (is (= t2 (desambiguar n2)))
  (is (= t3 (desambiguar n3)))
  (is (= t4 (desambiguar n4))))

(deftest test-precedencia
  (is (= (precedencia 'OR) 1))
  (is (= (precedencia 'AND) 2))
  (is (= (precedencia '*) 6))
  (is (= (precedencia '-u) 7))
  (is (= (precedencia 'MID$) 9)))

(deftest test-aridad
  (is (= (aridad 'THEN) 0))
  (is (= (aridad 'SIN) 1))
  (is (= (aridad '*) 2))
  (is (= (aridad 'MID$) 2))
  (is (= (aridad 'MID3$) 3)))

(deftest test-eliminar-cero-decimal
  (is (= 1.5 (eliminar-cero-decimal 1.5)))
  (is (= 1.5 (eliminar-cero-decimal 1.5)))
  (is (= 1 (eliminar-cero-decimal 1.0)))
  (is (= 'A (eliminar-cero-decimal 'A))))

(deftest test-eliminar-cero-entero
  (is (= nil (eliminar-cero-entero nil)))
  (is (= "A" (eliminar-cero-entero 'A)))
  (is (= "0" (eliminar-cero-entero 0)))
  (is (= "1.5" (eliminar-cero-entero 1.5)))
  (is (= "1" (eliminar-cero-entero 1)))
  (is (= "-1" (eliminar-cero-entero -1)))
  (is (= "-1.5" (eliminar-cero-entero -1.5)))
  (is (= ".5" (eliminar-cero-entero 0.5)))
  (is (= "-.5" (eliminar-cero-entero -0.5))))

(run-tests)
