import java.util.ArrayList;
import java.util.List;

public class ProducerConsumerDemo {

    static class SharedList {
        private final List<Integer> buffer = new ArrayList<>();
        private final int capacity;

        public SharedList(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void put(int value) throws InterruptedException {

            while (buffer.size() == capacity) {
            // REGIÃO CRÍTICA: condição/espera sobre o estado do buffer.
                wait();
            }

            // REGIÃO CRÍTICA: modificação do estado compartilhado.
            buffer.add(value);
            System.out.printf("[T1] produziu: %d (tam=%d)%n", value, buffer.size());

            notifyAll();
        }

        public synchronized int take() throws InterruptedException {

            while (buffer.isEmpty()) {
            // REGIÃO CRÍTICA: condição/espera sobre o estado do buffer.
                wait();
            }

            // REGIÃO CRÍTICA: modificação do estado compartilhado.
            int value = buffer.remove(0);
            System.out.printf("[T2] consumiu: %d (tam=%d)%n", value, buffer.size());

            notifyAll();
            return value;
        }
    }

    public static void main(String[] args) {
        final SharedList shared = new SharedList(5);
        final int TOTAL = 20;

        Thread T1 = new Thread(() -> {
            try {
                for (int i = 1; i <= TOTAL; i++) {
                    shared.put(i);         // REGIÃO CRÍTICA ocorre dentro de put
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "T1-Produtor");

        Thread T2 = new Thread(() -> {
            try {
                for (int i = 1; i <= TOTAL; i++) {
                    int v = shared.take(); // REGIÃO CRÍTICA ocorre dentro de take
                    Thread.sleep(80);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "T2-Consumidor");

        T1.start();
        T2.start();

        try {
            T1.join();
            T2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Fim.");
    }
}