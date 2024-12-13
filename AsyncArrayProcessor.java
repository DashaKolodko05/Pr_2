import java.util.Random;
import java.util.concurrent.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Scanner;

public class AsyncArrayProcessor {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Введення параметрів від користувача
        System.out.print("Введіть множник: ");
        int multiplier = scanner.nextInt();

        // Введення діапазону чисел
        int lowerBound, upperBound;
        while (true) {
            System.out.print("Введіть мінімум діапазону (-100 <= мінімум <= 100): ");
            lowerBound = scanner.nextInt();

            System.out.print("Введіть максимум діапазону (-100 <= максимум <= 100): ");
            upperBound = scanner.nextInt();

            if (lowerBound >= -100 && upperBound <= 100 && lowerBound < upperBound) {
                break;
            } else {
                System.out.println("Некоректний діапазон! Будь ласка, введіть значення в межах [-100; 100] і переконайтесь, що мінімум менший за максимум.");
            }
        }

        // Введення кількості елементів масиву
        System.out.print("Введіть кількість елементів у масиві (від 40 до 60): ");
        int arraySize = scanner.nextInt();

        if (arraySize < 40 || arraySize > 60) {
            System.out.println("Невірна кількість елементів!");
            return;
        }

        // Створення та заповнення масиву рандомними числами в заданому діапазоні
        Random random = new Random();
        int[] array = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            array[i] = random.nextInt(upperBound - lowerBound + 1) + lowerBound;
        }

        // Виведення початкового масиву
        System.out.println("Початковий масив:");
        for (int num : array) {
            System.out.print(num + " ");
        }
        System.out.println();

        // Ініціалізація потокового виконавця
        int numThreads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<int[]>> futures = new CopyOnWriteArrayList<>();

        long startTime = System.nanoTime();

        // Розбиваємо масив на частини та відправляємо кожну частину на обробку в окремий потік
        for (int i = 0; i < numThreads; i++) {
            final int start = i * (arraySize / numThreads);
            final int end = (i == numThreads - 1) ? arraySize : (i + 1) * (arraySize / numThreads);

            Callable<int[]> task = () -> {
                int[] part = new int[end - start];
                for (int j = start; j < end; j++) {
                    part[j - start] = array[j] * multiplier;
                }
                return part;
            };

            futures.add(executorService.submit(task));
        }

        // Збір результатів і перевірка стану кожного Future
        int[] result = new int[arraySize];
        try {
            int resultIndex = 0;
            for (Future<int[]> future : futures) {
                if (future.isCancelled()) {
                    System.out.println("Один з потоків було скасовано.");
                } else {
                    int[] part = future.get(); // Блокує до завершення
                    System.arraycopy(part, 0, result, resultIndex, part.length);
                    resultIndex += part.length;
                }

                if (!future.isDone()) {
                    System.out.println("Попередження: Один із потоків ще виконується!");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // Виведення обробленого масиву
        System.out.println("Оброблений масив:");
        for (int num : result) {
            System.out.print(num + " ");
        }
        System.out.println();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000; // Переведення в мілісекунди
        System.out.println("\nЧас виконання: " + duration + " мс");

        executorService.shutdown();
    }
}
