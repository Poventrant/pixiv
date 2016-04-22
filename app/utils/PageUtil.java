package utils;

import java.util.HashMap;
import java.util.Map;

public class PageUtil {

    /**
     * Use the origin page to create a new page
     *
     * @param page
     * @param totalRecords
     * @return
     */
    public Page createPage(Page page, int totalRecords) {
        return createPage(page.getEveryPage(), page.getCurrentPage(), totalRecords, page.getWidth());
    }

    /**
     * the basic page utils not including exception handler
     *
     * @param everyPage
     * @param currentPage
     * @param totalRecords
     * @return page
     */
    public Page createPage(int everyPage, int currentPage, int totalRecords, int width) {
        everyPage = getEveryPage(everyPage);
        currentPage = getCurrentPage(currentPage);
        int beginIndex = getBeginIndex(everyPage, currentPage);
        int totalPage = getTotalPage(everyPage, totalRecords);
        boolean hasNextPage = hasNextPage(currentPage, totalPage);
        boolean hasPrePage = hasPrePage(currentPage);
        Map<String, Integer> map = getBeginAndEnd(everyPage, totalRecords, width, currentPage);
        Integer begin = map.get("begin");
        Integer end = map.get("end");
        return new Page(hasPrePage, hasNextPage, everyPage, totalPage, currentPage, beginIndex, totalRecords, begin, end);
    }

    private int getEveryPage(int everyPage) {
        return everyPage == 0 ? 10 : everyPage;
    }

    private int getCurrentPage(int currentPage) {
        return currentPage == 0 ? 1 : currentPage;
    }

    private int getBeginIndex(int everyPage, int currentPage) {
        return (currentPage - 1) * everyPage;
    }

    private int getTotalPage(int everyPage, int totalRecords) {
        int totalPage = 0;

        if (totalRecords % everyPage == 0)
            totalPage = totalRecords / everyPage;
        else
            totalPage = totalRecords / everyPage + 1;

        return totalPage;
    }

    private boolean hasPrePage(int currentPage) {
        return currentPage == 1 ? false : true;
    }

    private boolean hasNextPage(int currentPage, int totalPage) {
        return currentPage == totalPage || totalPage == 0 ? false : true;
    }

    private Map<String, Integer> getBeginAndEnd(int everyPage, int totalRecords, int width, int currentPage) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        int begin = 0;
        int end = 0;
        int allPage = getTotalPage(everyPage, totalRecords);
        if (allPage <= width) {
            begin = 1;
            end = allPage;
        } else {
            begin = currentPage - 4;
            end = currentPage + 5;
            if (begin <= 0) {
                begin = 1;
                end = width;
            }
            if (end > allPage) {
                end = allPage;
                begin = end - 9;
            }
        }
        map.put("begin", begin);
        map.put("end", end);
        return map;
    }
}