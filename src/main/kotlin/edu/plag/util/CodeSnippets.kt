package edu.plag.util

class CodeSnippets {
    companion object {
        fun shortCode() = """
            public class Test {
                public static void main(String[] args) {
                    int x = 10.0;
                    String text = "Hello, World!";
                    String text = "Hello, World!";
                    String text = "Hello, World!";
                    String text = "Hello, World!";
                    System.out.println(text);
                }
            }
        """.trimIndent()

        fun shortCodeChanged() = """
            public class Test {
                public static void main(String[] args) {
                    String text = "Hello, World!";
                    String text = "Hello, World!";
                    System.out.println(text);
                }
            }
        """.trimIndent()

        fun longCode() = """
            public class StudentManagement {
                static class Student {
                    String name;
                    int age;
                    double gpa;
            
                    Student(String name, int age, double gpa) {
                        this.name = name;
                        this.age = age;
                        this.gpa = gpa;
                    }
                }
            
                public static void main(String[] args) {
                    List<Student> students = new ArrayList<>();
                    students.add(new Student("Alice", 20, 3.8));
                    students.add(new Student("Bob", 21, 3.2));
                    students.add(new Student("Charlie", 22, 3.5));
            
                    students.sort(Comparator.comparingDouble(s -> -s.gpa));
            
                    for (Student s : students) {
                        System.out.println(s.name + " - " + s.gpa);
                    }
                }
            }
        """.trimIndent()

        fun longCodeRenamed() = """
            public class Schoolmate {
                static class Schoolmate {
                    String alias;
                    int old;
                    double score;
            
                    Schoolmate(String alias, int old, double score) {
                        this.alias = alias;
                        this.old = old;
                        this.score = score;
                    }
                }
            
                public static void main(String[] args) {
                    List<Schoolmate> students = new ArrayList<>();
                    students.add(new Schoolmate("Alice", 20, 3.8));
                    students.add(new Schoolmate("Bob", 21, 3.2));
                    students.add(new Schoolmate("Charlie", 22, 3.5));
            
                    students.sort(Comparator.comparingDouble(s -> -s.score));
            
                    for (Schoolmate s : students) {
                        System.out.println(s.alias + " - " + s.score);
                    }
                }
            }
        """.trimIndent()

        fun canonicalBSearch() = """
            public class BinarySearch {
                public static int binarySearch(int[] arr, int target) {
                    int left = 0, right = arr.length - 1;
                    while (left <= right) {
                        int mid = left + (right - left) / 2;
                        if (arr[mid] == target)
                            return mid;
                        else if (arr[mid] < target)
                            left = mid + 1;
                        else
                            right = mid - 1;
                    }
                    return -1;
                }
            }
        """.trimIndent()

        fun trashBSearch() = """
            public class BinarySearch {
                public static int binarySearch(int[] arr, int target) {
                    int left = 0, right = arr.length - 1;
                    int unused = 42;
                    while (left <= right) {
                        int mid = left + (right - left) / 2;
                        System.out.println("Checking index: " + mid);
                        if (arr[mid] == target)
                            return mid;
                        else if (arr[mid] < target)
                            left = mid + 1;
                        else
                            right = mid - 1;
                    }
            
                    return -1;
                }
            }
        """.trimIndent()

        fun renameBSearch() = """
            public class SearchAlgo {
                public static int findElement(int[] numbers, int key) {
                    int low = 0, high = numbers.length - 1;
                    while (low <= high) {
                        int middle = low + (high - low) / 2;
                        if (numbers[middle] == key)
                            return middle;
                        else if (numbers[middle] < key)
                            low = middle + 1;
                        else
                            high = middle - 1;
                    }
                    return -1;
                }
            }
        """.trimIndent()

        fun restructuredBSearch() = """
            public class BinarySearch {
                public static int binarySearch(int[] arr, int target, int left, int right) {
                    if (left > right)
                        return -1;
                    int mid = left + (right - left) / 2;
                    if (arr[mid] == target)
                        return mid;
                    if (arr[mid] < target)
                        return binarySearch(arr, target, mid + 1, right);
                    return binarySearch(arr, target, left, mid - 1);
                }
            }
        """.trimIndent()
    }
}